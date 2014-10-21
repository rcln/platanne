/*  Take as input a lexicon with 2 fields:
 *   - word
 *   - phon
 *   and output a lexicon with:
 *   word \t phon1 phon2 ....
 *   AND add a variant with 'pause'
 *   AND replace the pp, tt, kk by: Bpp Opp ....
 *   AND replace the ee by eu
 *   AND replace the ## by pause
 *   AND replace the gn by nnyy
 *   AND replace the ng by nngg
 *   */
/*  FRED 0703  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>

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

/* phone set */

char *T_phoneset[]=
	{
		"pp", "tt", "kk", "aa", "ai", "an", "au","ee",
		"bb", "ch", "dd", "ei", "eu", "ff", "gg", "ii", "in", "jj",
		"kk", "ll", "mm", "nn", "gn", "ng", "oe", "on", "oo", "ou", "pause", "rr",
		"ss", "un", "uu", "uy", "vv", "ww", "yy", "zz", "##", ""
    };

/*................................................................*/

void print_phon(char *word, char *phon)
{
int i,j;

for(i=0;(phon[i])&&(phon[i+1]);i+=2)
 {
 if (i!=0) printf(" ");
 for(j=0;(T_phoneset[j][0])&&(strncmp(T_phoneset[j],phon+i,2));j++);
 if (T_phoneset[j][0]=='\0') { fprintf(stderr,"ERREUR: Bad phoneme (%c%c) in %s\n",phon[i],phon[i+1],word); exit(0); }
 if ((!strncmp(phon+i,"pp",2))||(!strncmp(phon+i,"tt",2))||(!strncmp(phon+i,"kk",2)))
  printf("O%c%c B%c%c",phon[i],phon[i+1],phon[i],phon[i+1]);
 else
  if (!strncmp(phon+i,"ee",2)) printf("eu");
  else
  if (!strncmp(phon+i,"gn",2)) printf("nn yy");
  else
  if (!strncmp(phon+i,"ng",2)) printf("nn gg");
  else
  if (!strncmp(phon+i,"##",2)) printf("pause");
  else printf("%c%c",phon[i],phon[i+1]);
 }
}

int main(int argc, char **argv)
{
char ch[TailleLigne],*word,*phon;
int nb,sans_pause;

if ((argc>1)&&(!strcmp(argv[1],"-pause"))) sans_pause=True; else sans_pause=False;

for(nb=0;fgets(ch,TailleLigne,stdin);nb++)
 {
 if ((word=strtok(ch," \t"))==NULL) ERREUR("bad format1:",ch);
 if ((phon=strtok(NULL," \t\n"))==NULL) ERREUR("bad format2:",ch);
 printf("%s\t",word); print_phon(word,phon); printf("\n");
 if (sans_pause==False)
  { printf("%s\t",word); print_phon(word,phon); printf(" pause\n"); }
 }
}
  
