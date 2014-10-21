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
/*  Decoupe un texte tokenise par DecoupeMots en phrases
    en accord avec un certain nombre d'heuristiques
    Les separateurs de phrases sont :
    <s>  : debut de phrase
    </s> : fin de phrase
    Entree : stdin
    Sortie : stdout  */
/*  FRED 0199  - last modif 07/2010 (nocap)  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <GestionTablMots.h>

#define TailleLigne	8000

/* modif 07/2010 option -nocap to process files without capital letters. */
int IF_nocap;

/* Gestion de la liste de mots necessitant de ne pas considere le '.' comme separateur */

char CharSeparateur;

char TablSeparateur[]=
	{
	'.', '!', '?',':',		/* separateur 'classique' de fin de phrase */
        ';', 				/* on rajoute le ; (DUC 0106) */
	'<','>',			/* separateur pour fichier HTML */
	'\0'
	};

int SiSeparateur(c)
 char c;
{
static int n;
for(n=0;TablSeparateur[n];n++) if (c==TablSeparateur[n]) return 1;
return 0;
}

int MotSeparateur(ch)
 char *ch;
{
if ((ch[1]=='\0')&&(SiSeparateur(ch[0]))) return 1;
if (!strcmp(ch,"...")) return 1;
return 0;
}

#define SiMajuscule(a)	((((a)>='A')&&((a)<='Z'))?1:0)
#define SiChiffre(a)	((((a)>='0')&&((a)<='9'))?1:0)
#define SiUnite(a)	((((a)=='%')||((a)=='$')||((a)=='F')||((a)==',')||((a)=='.'))?1:0)
#define SiDebutBloc(a)	((((a)=='[')||((a)=='(')||((a)=='"'))?1:0)
#define SiFinBloc(a)	((((a)==']')||((a)==')')||((a)=='"'))?1:0)

int DebutParagraphe(c)
 char c;
{
if ((c=='-')||(c=='*')||(c=='(')||(c=='[')||(c=='"')||(c=='\'')||(c=='<')||(c=='>')) return 1;
return 0;
}

/*  Definition de la fenetre d'examen  */

/* On regarde 5 mots avant et 5 mots apres */
#define TailleFenetre	11
#define SpotFenetre	5

#define TailleMot	200

char *Fenetre[TailleFenetre];
char Phrase[TailleFenetre][TailleMot];
int NeedStart;

/*  Heuristiques sur la fin de phrase  */

int CaraDebutPossible(ch)
 char *ch;
{
/* un cara est un debut possible s'il commence :
    * par une majuscule
    * par un chiffre
    * par un caractere debut_paragraphe */
if ((IF_nocap)||(SiMajuscule(*ch))) return 1;
if (SiChiffre(*ch)) return 1;
if (DebutParagraphe(*ch)) return 1;
return 0;
}

int JustDigit(ch)
 char *ch;
{
int n;
for(n=0;ch[n];n++) if (!SiChiffre(ch[n])) return 0;
return 1;
}

int MotDebutPossible(ch)
 char *ch;
{
/* un mot est un debut possible s'il est compose :
    * une majuscule ( NON PLUS MAINTENANT: suivie de minuscule)
    * un nombre
    * par un caractere debut_paragraphe */
int n;

if ((IF_nocap)||(SiMajuscule(*ch)))
 {
 return 1;
 /*  MODIF FRED NOVEMBRE 2003
 for(n=1;(ch[n])&&(!(SiMajuscule(ch[n])));n++);
 if (ch[n]=='\0') return 1; */
 }
if (SiChiffre(*ch))
 {
 for(n=1;(ch[n])&&(SiChiffre(ch[n])||(SiUnite(ch[n])));n++);
 if (ch[n]=='\0') return 1;
 }
if (DebutParagraphe(*ch)) return 1;
return 0;
}

int TraiteFinPhrase()
{
/* si on a W . avec W tout mot d'une seule lettre, c'est sans doute un sigle, pas de fin de phrase (edylex juillet 2010) */
if ((strlen(Fenetre[SpotFenetre-1])==1)&&(Fenetre[SpotFenetre][0]=='.')&&(MotDebutPossible(Fenetre[SpotFenetre+1])))
 { return 0; }

/* si on a [ digit : ] ou [ : digit ] alos pas de fin phrase */
if ((Fenetre[SpotFenetre][0]==':')&&((JustDigit(Fenetre[SpotFenetre-1]))||(JustDigit(Fenetre[SpotFenetre+1]))))
 return 0;
	
/* si on a [. \n \n ou plus] alors  fin_phrase */
if ((Fenetre[SpotFenetre+1][0]=='\n') &&
    (Fenetre[SpotFenetre+1][1]>=2))
	{ strcat(Fenetre[SpotFenetre],"\n</s>\n"); NeedStart=1; return 1; }

/* si . puis ' ou ` alors fin de phrase (DUC2006) */
if ((Fenetre[SpotFenetre][0]=='.')&&(Fenetre[SpotFenetre+1][0]=='`')) { strcat(Fenetre[SpotFenetre],"\n</s>\n"); NeedStart=1; return 1; }
if ((Fenetre[SpotFenetre][0]=='.')&&(Fenetre[SpotFenetre-1][0]=='\'')) { strcat(Fenetre[SpotFenetre],"\n</s>\n"); NeedStart=1; return 1; }
if ((Fenetre[SpotFenetre][0]=='.')&&(Fenetre[SpotFenetre+1][0]=='\'')&&(Fenetre[SpotFenetre+2][0]=='\''))
	{ strcat(Fenetre[SpotFenetre+2],"\n</s>\n"); NeedStart=1; return 1; }
if ((Fenetre[SpotFenetre][0]=='.')&&(Fenetre[SpotFenetre+1][0]=='\'')) { strcat(Fenetre[SpotFenetre+1],"\n</s>\n"); NeedStart=1; return 1; }

/* mot pouvant suivre une sequence chif . chif mot (sec, percent, ....) */
/* pour DUC2006 : si chif . chif alors pas de fin de phrase */
if ((Fenetre[SpotFenetre][0]=='.')&&(JustDigit(Fenetre[SpotFenetre-1]))&&(JustDigit(Fenetre[SpotFenetre+1])))
	return 0;
if ((JustDigit(Fenetre[SpotFenetre-1]))&&(JustDigit(Fenetre[SpotFenetre+1])) &&
    (JustPresent(Fenetre[SpotFenetre+2])))
	return 0;
if ((JustDigit(Fenetre[SpotFenetre-1]))&&(JustDigit(Fenetre[SpotFenetre+1])) &&
    (Fenetre[SpotFenetre+2][0]=='\n')&&(JustPresent(Fenetre[SpotFenetre+3])))
	return 0;

/* si on a [ M ou MM ou Mr ou Mrs ou Ms ou Mlle ou Dr ou Sen ou Pres ou Lt ou Gen ou hon une majuscule toute seule . majuscule] alors pas de fin de phrase */
if (((!strcmp(Fenetre[SpotFenetre-1],"M"))||(!strcmp(Fenetre[SpotFenetre-1],"MM"))||(!strcmp(Fenetre[SpotFenetre-1],"Gen"))||
	(!strcmp(Fenetre[SpotFenetre-1],"Gov"))||(!strcmp(Fenetre[SpotFenetre-1],"Atty"))||(!strcmp(Fenetre[SpotFenetre-1],"Ms"))||
	(!strcmp(Fenetre[SpotFenetre-1],"Mrs"))||(!strcmp(Fenetre[SpotFenetre-1],"Mlle"))||(!strcmp(Fenetre[SpotFenetre-1],"Dr"))||
	(!strcmp(Fenetre[SpotFenetre-1],"Sen"))||(!strcmp(Fenetre[SpotFenetre-1],"Pres"))||(!strcmp(Fenetre[SpotFenetre-1],"Lt"))||
	(!strcmp(Fenetre[SpotFenetre-1],"hon"))||
	((strlen(Fenetre[SpotFenetre-1])==1)&&(Fenetre[SpotFenetre-1][0]>='A')&&(Fenetre[SpotFenetre-1][0]<='Z'))||
	(!strcmp(Fenetre[SpotFenetre-1],"Mr")))&& (MotDebutPossible(Fenetre[SpotFenetre+1])))
 return 0;

/* si on a [Jan ou Feb ou Apr ou Jun ou Jul ou Aug ou Sept ou Sep ou Oct ou Nov ou Dec . chiffre] alors pas de fin de phrase */
if (((!strcmp(Fenetre[SpotFenetre-1],"Jan"))||(!strcmp(Fenetre[SpotFenetre-1],"Feb"))||
	(!strcmp(Fenetre[SpotFenetre-1],"Apr"))||(!strcmp(Fenetre[SpotFenetre-1],"Jun"))||(!strcmp(Fenetre[SpotFenetre-1],"Jul"))||
	(!strcmp(Fenetre[SpotFenetre-1],"Aug"))||(!strcmp(Fenetre[SpotFenetre-1],"Sept"))||(!strcmp(Fenetre[SpotFenetre-1],"Sep"))||
	(!strcmp(Fenetre[SpotFenetre-1],"Oct"))||(!strcmp(Fenetre[SpotFenetre-1],"Nov"))||(!strcmp(Fenetre[SpotFenetre-1],"Dec"))) &&
        ((SiChiffre(Fenetre[SpotFenetre+1][0]))||((Fenetre[SpotFenetre+1][0]=='\n')&&(SiChiffre(Fenetre[SpotFenetre+2][0])))))
 return 0;

/* si on a [ M ou MM . \n majuscule] alors pas de fin de phrase */
if (((!strcmp(Fenetre[SpotFenetre-1],"M"))||(!strcmp(Fenetre[SpotFenetre-1],"MM"))||
	(!strcmp(Fenetre[SpotFenetre-1],"Mr")))&& (Fenetre[SpotFenetre+1][0]=='\n') &&
	(MotDebutPossible(Fenetre[SpotFenetre+2])))
 return 0;

/* si on a [. \n cara_debu_possible] alors  fin_phrase */
/* si on a [. \n cara_debu_possible] alors  fin_phrase */
if ((Fenetre[SpotFenetre+1][0]=='\n') &&
    (CaraDebutPossible(Fenetre[SpotFenetre+2])))
	{ strcat(Fenetre[SpotFenetre],"\n</s>\n"); NeedStart=1; return 1; }

/* si on a [chif . chif quantificateur alors pas de fin de phrase */
/* si on a [. mot_debut_possible] alors  fin_phrase */
if (MotDebutPossible(Fenetre[SpotFenetre+1]))
	{ strcat(Fenetre[SpotFenetre],"\n</s>\n"); NeedStart=1; return 1; }

/* si on a [. fin_bloc \n cara_debu_possible] alors  fin_phrase */
if ((SiFinBloc(Fenetre[SpotFenetre+1][0])) &&
    (Fenetre[SpotFenetre+2][0]=='\n') &&
    (CaraDebutPossible(Fenetre[SpotFenetre+3])))
	 { strcat(Fenetre[SpotFenetre+1],"\n</s>\n"); NeedStart=1; return 1; }

/* si on a [. fin_bloc/debut_bloc mot_debut_possible] alors  fin_phrase */
if (((SiDebutBloc(Fenetre[SpotFenetre+1][0]))||(SiFinBloc(Fenetre[SpotFenetre+1][0]))) &&
    (MotDebutPossible(Fenetre[SpotFenetre+2])))
	 { strcat(Fenetre[SpotFenetre+1],"\n</s>\n"); NeedStart=1; return 1; }

/* si on a [. \n debut_bloc cara_debu_possible] alors  fin_phrase */
if ((SiDebutBloc(Fenetre[SpotFenetre+2][0])) &&
    (Fenetre[SpotFenetre+1][0]=='\n') &&
    (CaraDebutPossible(Fenetre[SpotFenetre+3])))
	 { strcat(Fenetre[SpotFenetre],"\n</s>\n"); NeedStart=1; return 1; }

return 1;
}

void TraiteLigne(ch)
 char *ch;
{
static int i_phrase=TailleFenetre,taille=0,last_saut=-1,just_open=1;
int i,j,n,l;
char *pt;

 for(pt=strtok(ch," \t");pt;pt=strtok(NULL," \t"))
  {
  i_phrase=i_phrase%TailleFenetre;
  for(i=0;i<TailleFenetre-1;i++) Fenetre[i]=Fenetre[i+1];
  Fenetre[i]=Phrase[i_phrase];

  /* on recopie pt dans Phrase[i_phrase] en enlevant \n sauf s'il est seul 
     car c'est un indice pour le decoupage en ligne et on comptabilise le nb de \n consecutifs */
  if (NeedStart)
   {
   NeedStart=0;
   strcpy(Phrase[i_phrase],"<s> ");
   }
  else Phrase[i_phrase][0]='\0';
   
  for(j=strlen(Phrase[i_phrase]),n=0;(pt[n])&&(pt[n]!='\n');n++,j++) Phrase[i_phrase][j]=pt[n];
  if (pt[n]=='\n')
   if (n>0) { Phrase[i_phrase][j]='\0'; last_saut=-1; }
   else
    {
    if (last_saut==-1)
     { Phrase[i_phrase][j]='\n'; Phrase[i_phrase][j+1]='\1'; Phrase[i_phrase][j+2]='\0'; last_saut=i_phrase; /*fprintf(stderr,"premier saut\n");*/ }
    else /* on incremente le compteur */
     { Phrase[last_saut][strlen(Phrase[last_saut])-1]++; Phrase[i_phrase][0]='\0'; /*fprintf(stderr,"\t saut num : %d\n",Phrase[last_saut][1]);*/ }
    }
  else { Phrase[i_phrase][j]='\0'; last_saut=-1; }
  i_phrase++;

  if (MotSeparateur(Fenetre[SpotFenetre])) TraiteFinPhrase();
  if ((Fenetre[SpotFenetre][0])&&(Fenetre[SpotFenetre][0]!='\n'))
   {
   printf("%s",Fenetre[SpotFenetre]);
   l=strlen(Fenetre[SpotFenetre]);
   /* si les 4 derniers caracteres affiche sont : <s>\n alors, just_open=1, pour eviter les lignes vides */
   if (!strncmp(&(Fenetre[SpotFenetre][l-4]),"<s>\n",4)) just_open=1; else just_open=0;
   if (Fenetre[SpotFenetre][l-1]=='\n')
    taille=0;
   else
    {
    taille+=l;
    if (taille>54) { printf("\n"); taille=0; }
    else printf("%c",CharSeparateur);
    }
   }
  else
   if (Fenetre[SpotFenetre][strlen(Fenetre[SpotFenetre])-2]=='\n')
    {
    if ((just_open==0)&&(Fenetre[SpotFenetre][strlen(Fenetre[SpotFenetre])-1]>=1)) { printf("\n</s>\n"); NeedStart=1; }
    }
/*
    {
    *fprintf(stderr,"saut de lignes consecutifs = %d\n",Fenetre[SpotFenetre][1]);*
    if (*(just_open==0) &&* (Fenetre[SpotFenetre][1]>=1))
    * on insere une fin de phrase si on ne vient pas d'ouvrir une ligne => WHY ?? 09/2010 * * before >=3 but why ?? 09/2010 *
     {
     if (just_open) Fenetre[SpotFenetre][1]--; * if there was just before an end of sentence, we don't add another linebreak *
     for(i=0;i<=Fenetre[SpotFenetre][1];i++)* WHY???? jan2012* printf("\n</s>\n<s>\n");
     }
    }
*/
  }
}

int main(argc,argv)
 int argc;
 char **argv;
{
char ch[TailleLigne];
int i,nb;

CharSeparateur=' '; /* par defaut, phrase formatees en paragraphe
		       ATTENTION : si on utilise lia_nett_capital
		       il FAUT que les phrases soient codees en paragraphe
		       et non pas 1 mot par ligne !!!! */

if (argc>1)
 {
 if (!strcmp(argv[1],"-h"))
  {
  fprintf(stderr,"Syntaxe : %s [-h] [-l] <list mot virgule> < stdin > stdout\n\
  \t Decoupe un texte en phrases tokenise (par exemple par la\n\
  \t commande 'lia_token') en accord avec un certain nombre\n\
  \t d'heuristiques codes dans la fonction 'TraiteFinPhrase'.\n\
  \t Les separateurs de phrases sont : '<s>' = debut de phrase\n\
  \t et '</s>' = fin de phrase.\n\
  \t Le fichier <list mots virgule compil> contient un lexique\n\
  \t compile avec la commande 'lia_compile_lexitree'. Ce lexique contient\n\
  \t tous les mots 'M' tel que, une expression de la forme :\n\
  \t '<nombre1> . <nombre2> M' ne sera pas decoupee en phrase \n\
  \t ATTENTION : lia_nett_capital n'accepte que les phrases structurees en paragraphe.\n\
  \t L'option '-nocap' permet de specifier que le texte en entree n'a aucune majuscule\n",argv[0]);
  exit(0);
  }
 if ((argc>2)&&(!strcmp(argv[2],"-l"))) CharSeparateur='\n';
 }
else
 {
 fprintf(stderr,"Syntaxe : %s [-h] [-l] <list mot virgule> < stdin > stdout\n",argv[0]);
 exit(0);
 }

IF_nocap=0;

if (argc>1)
 {
 for(nb=1;nb<argc;nb++)
  if (!strcmp(argv[nb],"-nocap"))
   {
   IF_nocap=1;
   }
  else ChargeLexiqueCompile(argv[1]);
 }
else { fprintf(stderr,"ERREUR: file <list mot virgule> missing !!\n"); exit(0); }

/* Initialisation de la phrase */
for(i=0;i<TailleFenetre;i++)
 {
 strcpy(Phrase[i],"");
 Fenetre[i]=Phrase[i];
 }

NeedStart=1;

while(fgets(ch,TailleLigne,stdin)) TraiteLigne(ch);

/* On vide la phrase */
strcpy(ch,"\n");
for(i=0;i<=SpotFenetre;i++) TraiteLigne(ch);

/* dernier tag de fin de phrase */
printf("</s>\n");
exit(0);
}
  
