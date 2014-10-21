/*
#    --------------------------------------------------------
#    LIA_TAGG: a statistical POS tagger + syntactic bracketer
#    --------------------------------------------------------
#
#    Copyright (C) 2001 FREDERIC BECHET
#
#    ..................................................................
#
#    This file is part of LIA_TAGG
#
#    LIA_TAGG is free software; you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation; either version 2 of the License, or
#    (at your option) any later version.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program; if not, write to the Free Software
#    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
#    ..................................................................
#
#    Contact :
#              FREDERIC BECHET - LIA - UNIVERSITE D'AVIGNON
#              AGROPARC BP1228 84911  AVIGNON  CEDEX 09  FRANCE
#              frederic.bechet@lia.univ-avignon.fr
#    ..................................................................
*/
/*  Decoupe un corpus en accord avec un dico compile
    par CompileLexiTree et une table de separateur hard-codee
    Entree : stdin pour le texte et 1er parametre pour le dico
    Sortie : stdout */
/*  FRED 0199  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>

/*  Definition des separateurs de mots  */

char TablSeparateur[]=
	{
	' ','-','_','\'','"','*','%','+','/',		/* separateur intra/inter mots */
	'.',';',',','?','!',':','<','>','«','»',	/* separateur inter mots */
	'\'','`','"',					/* separateur inter mots (DUC2006) */
	'(',')','[',']','{','}',			/* separateur phrase */
	'\n','\0'} ;					/* fin de phrase */

char TablSeparateurLazy[]=
	{
	' ','\'','"',					/* separateur intra/inter mots */
	'.',';',',','?','!',':','«','»',		/* separateur inter mots */
	'(',')','[',']','{','}',			/* separateur phrase */
	'\n','\0'} ;					/* fin de phrase */

int SiSeparateur(char c)
{
static int n;
if (c=='\0') return 1;
for(n=0;TablSeparateur[n];n++) if (c==TablSeparateur[n]) return 1;
return 0;
}

int SiSeparateurLazy(char c)
{
static int n;
if (c=='\0') return 1;
for(n=0;TablSeparateurLazy[n];n++) if (c==TablSeparateurLazy[n]) return 1;
return 0;
}

/*................................................................*/

/* src/format/GestionTablMots.c */

/*  Permet de charger un fichier lexique compile avec 'CompileLexiTree'
    puis d'utiliser la fonction 'Present' afin d'obtenir la plus
    longue chaine compatible avec le lexique  */
/*  FRED 0199  */

#define debug_GestionTablMots	0

typedef struct
	{
	char c,mot;
	unsigned int fg,fd;
	} type_stoktree;

type_stoktree *StockTree;

int NbNode;

#define SiIgnoreBlanc		1

/*  Chargement de l'arbre-tableau  */

void ChargeLexiqueCompile(ch)
 char *ch;
{
FILE *file;

if (!(file=fopen(ch,"rb")))
 { fprintf(stderr,"Can't open : %s\n",ch); exit(0); }

fseek(file,0,SEEK_END);
NbNode=(int)(ftell(file)/sizeof(type_stoktree));
fseek(file,0,SEEK_SET);
StockTree=(type_stoktree *)malloc(sizeof(type_stoktree)*(NbNode));
if (fread(StockTree,sizeof(type_stoktree),NbNode+1,file)!=NbNode)
 {
 fprintf(stderr,"Erreur lors de la lecture du fichier : %s\n",ch);
 exit(0);
 }
fclose(file);
}

/*  Decoupage  */

int EgalChar(c_test,c_arbre)
 char c_test,c_arbre;
{
if (SiIgnoreBlanc)
 if (((c_test==' ')||(c_test=='\n'))&&(c_arbre=='_')) return 1; /* MODIF FRED 11/2003 */

 /*if ((c_test==' ')&&((c_arbre=='_')*//*||(c_arbre=='-'))) return 1;*/
return 0;
}

/*  'Present' permet d'obtenir la plus longue chaine compatible avec
    le lexique. Les parametres sont les suivants :
    - ch	: chaine a analyser en entree
    - addr	: addresse dans l'arbre, a initialiser a 1
    - lastword	: renvoi l'adresse du dernier caractere de la chaine 'ch'
		  qui fait partie d'un mot du lexique
    - indice	: indice du caractere en cours, a initialiser a 0
    - refe	: tableau de caractere qui va contenir le mot reference
		  le plus long, dans le lexique, a partir de la chaine 'ch'
    - lastindice: indice de la fin du mot le plus long dans refe
    Si l'ensemble de la chaine 'ch' forme un mot du lexique, alors 'Present'
    retourne la valeur 1, et 0 sinon  */

int Present(char *ch, unsigned int addr, char **lastword, int indice, char *refe, int *lastindice, int if_lazy)
{
static int y;
if (addr==0) return 0;
if ((*ch==StockTree[addr].c)||(EgalChar(*ch,StockTree[addr].c)))
 {
 refe[indice]=StockTree[addr].c;
 if (debug_GestionTablMots)
  {
  for(y=0;y<=indice;y++) printf("%c",refe[y]);
  printf("\navec StockTree[addr].mot=%d ch[1]=[%c]\n",StockTree[addr].mot,ch[1]);
  }
 if (if_lazy)
  {
  if ((StockTree[addr].mot)&&((ch[1]=='\0')||(SiSeparateurLazy(ch[1]))||(SiSeparateurLazy(ch[0])))) { *lastword=ch; *lastindice=indice+1; }
  }
 else
  {
  if ((StockTree[addr].mot)&&((ch[1]=='\0')||(SiSeparateur(ch[1]))||(SiSeparateur(ch[0])))) { *lastword=ch; *lastindice=indice+1; }
  }
 if ((ch[1]=='\0')&&(StockTree[addr].mot)) return 1;
 return Present(ch+1,StockTree[addr].fg,lastword,indice+1,refe,lastindice,if_lazy);
 }
return Present(ch,StockTree[addr].fd,lastword,indice,refe,lastindice,if_lazy);
}

/*................................................................*/

#define TailleLigne	8000

void ProcessLine(char *ch, int if_lazy)	
{
char refe[TailleLigne],*lastword;
int lastindice,siblanc=0;

while ((*ch)&&(*ch!='\n'))
 {
 if (*ch!=' ')
  {
  lastword=NULL;
  Present(ch,1,&lastword,0,refe,&lastindice,if_lazy);
  if (lastword)
   {
   refe[lastindice]='\0';
   printf("%s ",refe);
   ch=lastword+1;
   siblanc=1;
   }
  else
   {
   /* Politique : on coupe les mots d'un qu'on trouve un separateur
      sauf si le separateur est inclu a un mot du dico */
   siblanc=0;
   if (if_lazy) while (!SiSeparateurLazy(*ch)) printf("%c",*ch++);
   else while (!SiSeparateur(*ch)) printf("%c",*ch++);
   if (*ch!=' ') printf(" %c ",*ch++);
   }
  }
 else
  {
  while (*ch==' ') ch++;
  if (siblanc==0) printf(" ");
  }
 }
if (*ch=='\n') printf("\n");
}

#define True	1
#define False	0

#define SIZE_BUFFER		800000
#define SIZE_LINE		80

#define BLANK(a)	(((a)==' ')||((a)=='\n')||((a)=='\t'))
#define CAPITAL(a)	(((a)>='A')&&((a)<='Z'))
#define NUMBER(a)	(((a)>='0')&&((a)<='9'))

#define MAX_SIZE_TAG	80

int format_ben(char *ch,char **lastword, char *refe, int *lastindice)
{
int i;

/* SGML tags <?*> */
if ((ch[0]=='<')&&(ch[1]!='>'))
 {
 refe[0]=ch[0];
 for(i=1;(ch[i])&&(i<MAX_SIZE_TAG)&&(ch[i]!='>')&&(ch[i]!='<');i++) refe[i]=ch[i];
 if (ch[i]=='>')
  {
  refe[i]=ch[i];
  refe[i+1]='\0';
  *lastindice=i+1;
  *lastword=(ch+i);
  return True;
  }
 else return False;
 }

/* forms starting with '$' */
if ((ch[0]!='$')||(!(CAPITAL(ch[1])))) return False;
refe[0]=ch[0];
refe[1]=ch[1];
for(i=2;(!BLANK(ch[i]))&&((CAPITAL(ch[i]))||(NUMBER(ch[i]))||(ch[i]=='_'));i++)
 refe[i]=ch[i];
if (BLANK(ch[i]))
 {
 *lastindice=i;
 *lastword=(ch+i-1);
 return True;
 }
else return False;
}

void ProcessBuffer(FILE *file, int if_ben, int keep_fmt, int if_lazy)
{
static char buffer[SIZE_BUFFER+1]; /* BUG FIXED: +1 !!!! FRED 0105 */
int nbread,fini,i;
char refe[TailleLigne],*lastword,*ch;
int lastindice,siblanc=0,t;

nbread=fread(buffer,sizeof(char),SIZE_BUFFER,file);

for(t=0,ch=buffer;ch!=buffer+nbread;)
 {
 if (!(BLANK(*ch)))
  {
  lastword=NULL;
  if ((if_ben==False)||(format_ben(ch,&lastword,refe,&lastindice)==False))
   Present(ch,1,&lastword,0,refe,&lastindice,if_lazy);
  if (lastword)
   {
   refe[lastindice]='\0';
   printf("%s ",refe);
   t+=strlen(refe);
   ch=lastword+1;
   siblanc=1;
   }
  else
   {
   /* Politique : on coupe les mots d'un qu'on trouve un separateur
      sauf si le separateur est inclu a un mot du dico */
   siblanc=0;
   if (if_lazy) while (!SiSeparateurLazy(*ch)) { printf("%c",*ch++); t++; }
   else while (!SiSeparateur(*ch)) { printf("%c",*ch++); t++; }
   if ((*ch!=' ')&&(*ch!='\n')) { printf(" %c ",*ch++); t+=3; }
   else
    if ((!keep_fmt)&&(*ch=='\n')) *ch=' ';
   }
  }
 else
  {
  while (BLANK(*ch)) { if (keep_fmt) printf("%c",*ch); ch++; }

  if (!keep_fmt)
   {
   if (t>SIZE_LINE) { t=0; printf("\n"); } else { printf(" "); t++; }
   }
  
  /* on est sur un <espace> on "vide" et re-rempli le buffer */
  if ((nbread==SIZE_BUFFER)&&((int)(ch-buffer)>(SIZE_BUFFER/2)))
   {
   /*printf("\nVIDE: nbread=%d processed=%d to be done = %d",
		   nbread,(int)(ch-buffer),SIZE_BUFFER-(int)(ch-buffer));*/

   for(i=0;i<SIZE_BUFFER-(int)(ch-buffer);i++) buffer[i]=ch[i];
   nbread=fread(buffer+i,sizeof(char),(int)(ch-buffer),stdin);
   ch=buffer;

   /*printf(" || nbread=%d new nbread=%d\n",nbread,nbread+i);*/

   nbread+=i;

   buffer[nbread]='\0';
   }
  }
 }
}



/*  Prog Principal  */

int main(argc,argv)
 int argc;
 char **argv;
{
char ch[TailleLigne];
int nb,if_ben,keep_fmt,if_lazy;

if (argc<2)
 {
 fprintf(stderr,"Syntaxe : %s [-h] [-ben] [-keep_fmt] [-lazy] <lexi compile>\n",argv[0]);
 exit(0);
 }

if (!strcmp(argv[1],"-h"))
 {
 fprintf(stderr,"Syntaxe : %s [-h] <lexi compile>\n\
 \t Decoupe un corpus en accord avec un dico compile\n\
 \t par CompileLexiTree et une table de separateur hard-codee\n\
 \t dans le tableau 'TablSeparateur'. La politique de segmentation\n\
 \t est la suivante : on concatene les mots suivant la plus grande\n\
 \t expression composee trouvee dans le dico. Lorsqu'un\n\
 \t mot contient un caractere separateur, s'il fait partie du\n\
 \t dico, on le laisse tel-quel, sinon on l'eclate autour\n\
 \t du caractere separateur.\n\
 \t Modif Novembre 2004: l'option '-ben' permet de ne pas tokeniser\n\
 \t les mots qui commencent par un '$' suivi d'une lettre majuscule\n\
 \t suivi d'une sequence quelconque de lettre majuscule, de '_' ou de\n\
 \t chiffres ainsi que toute les balises '<?*>'\n\
 \t Modif Novembre 2006: l'option '-lazy' permet de ne considerer comme separateurs\n\
 \t que les ponctuations: '.' ':' ',' ';' '!' '?' et c'est tout.\n\
 \t Entree : stdin pour le texte et 1er parametre pour le dico\n\
 \t Sortie : stdout\n",argv[0]);
 exit(0);
 }

/*fprintf(stderr,"Chargement de l'arbre dans le tableau -> ");*/
if_ben=keep_fmt=if_lazy=False;
for(nb=1;nb<argc;nb++)
 if (!strcmp(argv[nb],"-ben")) if_ben=True; else
 if (!strcmp(argv[nb],"-keep_fmt")) keep_fmt=True; else
 if (!strcmp(argv[nb],"-lazy")) if_lazy=True;
 else ChargeLexiqueCompile(argv[nb]);
/*fprintf(stderr,"Termine\n");*/
/*fprintf(stderr,"On a lu : %d noeuds\n",NbNode);*/
ProcessBuffer(stdin,if_ben,keep_fmt,if_lazy);
/* while(fgets(ch,TailleLigne,stdin)) ProcessLine(ch,if_lazy); */
return 0;
}
 
