/*  Split again the output of LIA_CLEAN !! according to MOSTFREQUENTSTART  */
/*  FRED 0112  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <lia_liblex.h>

/*................................................................*/

#define TailleLigne     8000

#define True    1
#define False   0

void DECOFREQ_ERREUR(char *ch1,char *ch2)
{
fprintf(stderr,"DECOFREQ_ERREUR : %s %s\n",ch1,ch2);
exit(0);
}

void DECOFREQ_ERREURd(char *ch1, int i)
{
fprintf(stderr,"DECOFREQ_ERREUR : %s %d\n",ch1,i);
exit(0);
}

/*................................................................*/

#define IF_NUMBER(a)	(((a)>='0')&&((a)<='9'))

int if_capital(char c)
{
if ((c>='A')&&(c<='Z')) return True;
if (
        (c=='É') ||
        (c=='À') ||
        (c=='È') ||
        (c=='Ù') ||
        (c=='Â') ||
        (c=='Ê') ||
        (c=='Î') ||
        (c=='Ô') ||
        (c=='Û') ||
        (c=='Ä') ||
        (c=='Ë') ||
        (c=='Ï') ||
        (c=='Ö') ||
        (c=='Ü') ||
        (c=='Ç')
   ) return True;
return False;
}

int if_lettre(char c)
{
if ((c>='A')&&(c<='Z')) return True;
if ((c>='a')&&(c<='z')) return True;
if (
        (c=='É') ||
        (c=='À') ||
        (c=='È') ||
        (c=='Ù') ||
        (c=='Â') ||
        (c=='Ê') ||
        (c=='Î') ||
        (c=='Ô') ||
        (c=='Û') ||
        (c=='Ä') ||
        (c=='Ë') ||
        (c=='Ï') ||
        (c=='Ö') ||
        (c=='Ü') ||
        (c=='Ç') ||
	(c=='é') ||
	(c=='à') ||
	(c=='è') ||
	(c=='ù') ||
	(c=='â') ||
	(c=='ê') ||
	(c=='î') ||
	(c=='ô') ||
	(c=='û') ||
	(c=='ä') ||
	(c=='ë') ||
	(c=='ï') ||
	(c=='ö') ||
	(c=='ü') ||
	(c=='ç')
   ) return True;
return False;
}

char decapital(char c)
{
if ((c>='A')&&(c<='Z')) return (c+('a'-'A'));
if (c=='É') return 'é';
if (c=='À') return 'à';
if (c=='È') return 'è';
if (c=='Ù') return 'ù';
if (c=='Â') return 'â';
if (c=='Ê') return 'ê';
if (c=='Î') return 'î';
if (c=='Ô') return 'ô';
if (c=='Û') return 'û';
if (c=='Ä') return 'ä';
if (c=='Ë') return 'ë';
if (c=='Ï') return 'ï';
if (c=='Ö') return 'ö';
if (c=='Ü') return 'ü';
if (c=='Ç') return 'ç';
return c;
}

char recapital(char c)
{
if ((c>='a')&&(c<='z')) return (c+('A'-'a'));
if (c=='é') return 'É';
if (c=='à') return 'À';
if (c=='è') return 'È';
if (c=='ù') return 'Ù';
if (c=='â') return 'Â';
if (c=='ê') return 'Ê';
if (c=='î') return 'Î';
if (c=='ô') return 'Ô';
if (c=='û') return 'Û';
if (c=='ä') return 'Ä';
if (c=='ë') return 'Ë';
if (c=='ï') return 'Ï';
if (c=='ö') return 'Ö';
if (c=='ü') return 'Ü';
if (c=='ç') return 'Ç';
return c;
}

char desaccentue(char c)
{
if (c=='é') return 'e';
if (c=='à') return 'a';
if (c=='è') return 'e';
if (c=='ù') return 'u';
if (c=='â') return 'a';
if (c=='ê') return 'e';
if (c=='î') return 'i';
if (c=='ô') return 'o';
if (c=='û') return 'u';
if (c=='ä') return 'a';
if (c=='ë') return 'e';
if (c=='ï') return 'i';
if (c=='ö') return 'o';
if (c=='ü') return 'u';
if (c=='ç') return 'c';
return c;
}

char *decapital_string(char *pt)
{
int i;
for(i=0;pt[i];i++) pt[i]=decapital(pt[i]);
return pt;
}

int strange_inside(char *ch)
{
int i;
for (i=0;ch[i];i++)
 {
 if ((!if_lettre(ch[i]))&&(ch[i]!='-')&&(ch[i]!='_')&&(ch[i]!='\'')&&(ch[i]!=' ')&&(ch[i]!='.'))
  {
  if (ch[i+1]) return True;
  }
 }
return False;
}

int just_capital(char *ch)
{
int i;
for (i=0;ch[i];i++)
 {
 if ((if_lettre(ch[i]))&&(!if_capital(ch[i]))) return False;
 }
return True;
}

int no_capital(char *ch)
{
int i;
for (i=0;ch[i];i++)
 {
 if ((if_lettre(ch[i]))&&(if_capital(ch[i]))) return False;
 }
return True;
}

int include_a2(char *ch)
{
int i;
for (i=0;ch[i];i++)
 {
 if ((ch[i]=='à')&&((i==0)||(ch[i-1]==' '))&&((ch[i+1]==' ')||(ch[i+1]=='\0'))) return True;
 }
return False;
}

int include_sequence_capital(char *ch)
{
static char chtmp[TailleLigne],*pt;
int i,ifroman;
strcpy(chtmp,ch);
for(pt=strtok(chtmp," -");pt;pt=strtok(NULL," -"))
 {
 for(ifroman=True,i=0;(pt[i])&&(if_capital(pt[i]));i++)
  {
  if ((pt[i]!='I')&&(pt[i]!='V')&&(pt[i]!='X')) ifroman=False;
  }
 if ((!pt[i])&&(!ifroman)) return True;
 }
return False;
}

void copy_lite(char *dest, char *source)
{
int i,j;

for(i=j=0;source[i];i++) if (if_lettre(source[i])) dest[j++]=desaccentue(decapital(source[i]));
dest[j]='\0';
if (0) printf("[%s] => [%s]\n",source,dest);
}

/*................................................................*/

void clean_pers(char *ch, char *chpers)
{
int i,j;

if (!if_capital(chpers[0])) chpers[0]=recapital(chpers[0]);
for(i=j=0;chpers[i];i++)
 {
 if (chpers[i]=='_') chpers[i]=' ';
 if (chpers[i]==' ')
  {
  if ((i>0)&&(chpers[i-1]!='-')&&(chpers[i-1]!='\'')&&(chpers[i-1]!=' ')&&(chpers[i+1]!='-')&&(chpers[i+1]!='\'')&&(chpers[i+1]!=' '))
   {
   ch[j++]=' ';
   if ((chpers[i+1])&&(!if_capital(chpers[i+1]))) chpers[i+1]=recapital(chpers[i+1]);
   }
  }
 else
 if (chpers[i]=='.')
  {
  ch[j++]=chpers[i];
  if ((chpers[i+1])&&(chpers[i+1]!=' ')) ch[j++]=' ';
  }
 else
 if ((chpers[i]=='-')||(chpers[i]=='\''))
  {
  ch[j++]=chpers[i];
  if ((chpers[i+1])&&(if_lettre(chpers[i+1]))&&(!if_capital(chpers[i+1]))) chpers[i+1]=recapital(chpers[i+1]);
  } 
 else ch[j++]=chpers[i];
 }
ch[j]='\0';
}

/*................................................................*/

void inverse_nom_prenom(char *ch, char *chpers)
{
int i,j;
for (j=i=0;(chpers[i])&&((if_capital(chpers[i]))||(chpers[i]==' ')||(chpers[i]=='_')||(chpers[i]=='-')||(chpers[i]=='\''));i++)
 if (chpers[i]==' ') j=i;
if ((!chpers[i])||(j==0)) DECOFREQ_ERREUR("bad format:",chpers);
chpers[j]='\0';
sprintf(ch,"%s %s",chpers+j+1,chpers);
chpers[j]=' ';
}

/*................................................................*/

char T_possible_end[]={'.','!','?','(','\0'};

int possible_end(char *ch)
{
int i,l;
if (!strcmp(ch,"<s>")) return True; /* if deb sentence, then it's a deb !! */
l=strlen(ch)-1;
for(i=0;(T_possible_end[i])&&(T_possible_end[i]!=ch[l]);i++);
if (T_possible_end[i]) return True;
else return False;
}

int main(int argc, char **argv)
{
int nb,lexid,i,nosplit,l;
char ch[TailleLigne],*pt,prevtoken[1000],chtmp[TailleLigne],c;

nosplit=False;
lexid=-1;
if (argc>1)
 for(nb=1;nb<argc;nb++)
  if (!strcmp(argv[nb],"-lex"))
   {
   if (nb+1==argc) DECOFREQ_ERREUR("must have a value after argument;",argv[nb]);
   lexid=load_lexicon(argv[++nb]);
   }
  else
  if (!strcmp(argv[nb],"-nosplit")) nosplit=True;
  else
  if (!strcmp(argv[nb],"-h"))
   {
   fprintf(stderr,"Syntax: %s [-h] -lex <file with most frequent start> [-nosplit]\n",argv[0]);
   exit(0);
   }
  else DECOFREQ_ERREUR("unknown option:",argv[nb]);

if (lexid==-1) DECOFREQ_ERREUR("bad syntax, check '-h'","");

for(prevtoken[0]='\0';fgets(ch,TailleLigne,stdin);)
 {
 pt=strtok(ch," \t\n\r");
 if (strlen(pt)>=1000) DECOFREQ_ERREUR("overflow (redecoupe_mostfreq)!! CH=",pt);
 if ((possible_end(prevtoken))&&(if_capital(pt[0])))
  {
  for(i=0;pt[i];i++) chtmp[i]=decapital(pt[i]);
  chtmp[i]='\0';
  /* HACK: le Ca en debut de phrase !! */
  if ((!strcmp(pt,"Ca"))||(word2code(lexid,chtmp,NULL))) /* we suppress the capital, and split again the sentence if needed */
   {
   if ((!nosplit)&&((l=strlen(prevtoken))>1)&&(strcmp(prevtoken,"<s>")))
    {
    c=prevtoken[l-1];
    sprintf(prevtoken+l-1,"\n%c",c);
    }
   printf("%s\n",prevtoken);
   if (!nosplit) { if (strcmp(prevtoken,"<s>")) sprintf(prevtoken,"</s>\n<s>\n"); }
   if (!strcmp(pt,"Ca")) strcat(prevtoken,"ça"); else strcat(prevtoken,chtmp);
   }
  else { printf("%s\n",prevtoken); strcpy(prevtoken,pt); }
  }
 else
  {
  if (prevtoken[0]) printf("%s\n",prevtoken);
  strcpy(prevtoken,pt);
  }
 }
if (prevtoken[0]) printf("%s\n",prevtoken);

exit(0);
}
 
