/*  Take as input a corpus already cut in sentences, with
 *  one word per line, and perform different rewriting
 *  according to a rule file  */
/*  FRED 0604  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <lia_tree_avl.h>

/*................................................................*/

#define TailleLigne     40000

#define True    1
#define False   0

void ERREUR(char *ch1,char *ch2)
{
fprintf(stderr,"ERREUR : %s %s\n",ch1,ch2);
exit(0);
}

/*................................................................*/

/* number */

char *TablNumber[]=
	{
	"zéro",
	"un",
	"deux",
	"trois",
	"quatre",
	"cinq",
	"six",
	"sept",
	"huit",
	"neuf",
	"dix",
	"onze",
	"douze",
	"treize",
	"quatorze",
	"quinze",
	"seize",
	"vingt",
	"trente",
	"quarante",
	"cinquante",
	"soixante",
	"quatre_vingt",
	"mille",
	"cent",
	"" 
	};

int if_number(char *pt)
{
int i;
for(i=0;(TablNumber[i][0])&&(strcmp(TablNumber[i],pt));i++);
return TablNumber[i][0]?True:False;
}

/*................................................................*/

#define MAX_CONTEXT		20
#define MAX_SIZE_WORD	2000

char T_context[MAX_CONTEXT][MAX_SIZE_WORD];

/*................................................................*/

/* rules */
/* format:
Raymond barre # Raymond Barre
<UNK> un <UNK> # </s> <s>
<s> m $CAPITAL # <s> monsieur $CAPITAL
*/

#define MAX_RULES	800

char *T_rules_in[MAX_RULES][MAX_CONTEXT];
char *T_rules_out[MAX_RULES][MAX_CONTEXT];

int NbRules;

void read_rules(char *filename)
{
FILE *file;
char ch[TailleLigne],*pt;
int i,nb,in;

NbRules=0;
if (!(file=fopen(filename,"rt"))) ERREUR("can't open:",filename);
for(nb=0;fgets(ch,TailleLigne,file);)
 if (ch[0]!='#')
  {
  for(i=0,in=True,pt=strtok(ch," \t\n");pt;pt=strtok(NULL," \t\n"),i++)
   {
   if (i>MAX_CONTEXT-2) ERREUR("cste MAX_CONTEXT too small:",ch);
   if (!strcmp(pt,"#")) { in=False; T_rules_in[nb][i]=NULL; i=-1; }
   else
    {
    if (in) T_rules_in[nb][i]=strdup(pt); else T_rules_out[nb][i]=strdup(pt); 
    }
   }
  T_rules_out[nb][i]=NULL;
  nb++;
  }
NbRules=nb;
fclose(file);
}

void print_rules()
{
int i,j;

for(i=0;i<NbRules;i++)
 {
 printf("# Rule %d:\n",i);
 for(j=0;T_rules_in[i][j];j++) printf("%s ",T_rules_in[i][j]);
 printf("# ");
 for(j=0;T_rules_out[i][j];j++) printf("%s ",T_rules_out[i][j]);
 printf("\n");
 }
}

/*................................................................*/

/*  Check rules  */

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

int check_rules(int *numrule)
{
int i,j,ok;
for(i=0;i<NbRules;i++)
 {
 for(j=0,ok=True;(ok)&&(j<MAX_CONTEXT)&&(T_rules_in[i][j]);j++)
  {
  if (strcmp(T_rules_in[i][j],T_context[j]))
   {
   /* check for non terminal */
   if (!strcmp(T_rules_in[i][j],"$CAPITAL"))
    {
    if (!(if_capital(T_context[j][0]))) ok=False;
    }
   if (!strcmp(T_rules_in[i][j],"$NUMBER"))
    {
    if (!(if_number(T_context[j]))) ok=False;
	}
   else ok=False;
   }
  }
 if (ok) { *numrule=i; return True; }
 }
return False;
}

/*................................................................*/

/* reading lexicon */

lia_avl_t read_lexicon(char *filename)
{
FILE *file;
static char ch[TailleLigne],*pt;
lia_avl_t resu;

if (!(file=fopen(filename,"rt"))) ERREUR("can't open:",filename);
for (resu=NULL;fgets(ch,TailleLigne,file);)
 for(pt=strtok(ch," \t\n");pt;pt=strtok(NULL," \t\n")) 
   resu=lia_ajoute_element_avl(resu,pt,True,NULL);
fclose(file);
return resu;
}

/*................................................................*/

/* rewrite */

void rewrite_context(int numrule, int *next)
{
int i,j,k;
/*
fprintf(stderr,"\tapply rule %d\n",numrule);
*/
for(i=j=0;T_rules_in[numrule][i];i++)
 if (T_rules_out[numrule][j])
  {
  if (!strcmp(T_rules_in[numrule][i],"$NUMBER"))
   {
   printf("%s\n",T_context[i]);
   }
  else
  if (!strcmp(T_rules_in[numrule][i],"$CAPITAL"))
   {
   if (strncmp(T_rules_out[numrule][j],"$CAPITAL",8))
    {
	fprintf(stderr,"ERROR: can't apply rule %d, sorry !! (mismatch between non terminal)\n",numrule+1);
	exit(0);
	}
   if (T_rules_out[numrule][j][8]!='\0') printf("%s%s\n",T_context[i],T_rules_out[numrule][j]+8);
   else printf("%s\n",T_context[i]);
   }
  else
  if (strstr(T_rules_out[numrule][j],"$CAPITAL"))
   {
   if (strcmp(T_rules_in[numrule][i+1],"$CAPITAL"))
    { fprintf(stderr,"ERROR: can't apply rule %d, sorry !!\n",numrule+1); exit(0); }
   for(k=0;strcmp(T_rules_out[numrule][j]+k,"$CAPITAL");k++) printf("%c",T_rules_out[numrule][j][k]);
   printf("%s\n",T_context[++i]);
   }
  else
   printf("%s\n",T_rules_out[numrule][j]);
  j++;
  }
while (T_rules_out[numrule][j]) printf("%s\n",T_rules_out[numrule][j++]);
*next=i;
}

/*................................................................*/

void process_corpus(lia_avl_t lexicon)
{
int i,numrule,next,pasfini,decompte,nb;
static char ch[TailleLigne],ch2[TailleLigne];

for(i=0;i<MAX_CONTEXT;i++) T_context[i][0]='\0';
for(pasfini=True,decompte=0;pasfini;)
 {
 /*
 fprintf(stderr,"process_corpus:\n");
 for(i=0;i<MAX_CONTEXT;i++) fprintf(stderr,"\tT_context[%d]=%s\n",i,T_context[i]);
 */
 if (decompte==0)
  {
  if (fgets(ch,TailleLigne,stdin))
   {
   strtok(ch," \t\n");
   if ((lexicon)&&(!lia_recherche_avl(lexicon,ch,&nb)))
    {
    strcpy(ch2,ch);
	for(i=0;ch2[i];i++) ch2[i]=decapital(ch2[i]);
	if (lia_recherche_avl(lexicon,ch2,&nb)) strcpy(ch,ch2);
	}
   }
  else { ch[0]='\0'; decompte=1; }
  }
 else
  {
  if (decompte==MAX_CONTEXT) pasfini=False; else decompte++;
  }
 if (strlen(ch)>=MAX_SIZE_WORD) ERREUR("cste MAX_SIZE_WORD too small","");
 for(i=0;i<MAX_CONTEXT-1;i++) strcpy(T_context[i],T_context[i+1]);
 strcpy(T_context[i],ch);

 if (check_rules(&numrule))
  {
  rewrite_context(numrule,&next);
  for(i=0;i<next;i++) T_context[i][0]='\0';
  }
 else if (T_context[0][0]) printf("%s\n",T_context[0]);
 }
}

int main(int argc, char **argv)
{
char ch[TailleLigne];
int nb;
lia_avl_t lexicon;

lexicon=NULL;

if (argc>1)
 for(nb=1;nb<argc;nb++)
  if (!strcmp(argv[nb],"-rule"))
   {
   if (nb+1==argc) ERREUR("a value must follow option:",argv[nb]);
   read_rules(argv[++nb]);
   } else
  if (!strcmp(argv[nb],"-capital"))
   {
   if (nb+1==argc) ERREUR("a value must follow option:",argv[nb]);
   lexicon=read_lexicon(argv[++nb]);
   } else
  if (!strcmp(argv[nb],"-h"))
   {
   fprintf(stderr,"Syntax: %s [-h] -rule <filename> -capital <lexicon>\n",argv[0]);
   exit(0);
   }
  else ERREUR("unknown option:",argv[nb]);
else ERREUR("check syntax (-h)","");
/*print_rules();*/
process_corpus(lexicon);
exit(0);
}
  
