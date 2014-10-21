/*  Extract from a speeral fmt lexicon, a sub lex defined
 *  by a wordlist given as parameter  */
/*  FRED 1204  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <lia_liblex.h>
#define TailleLigne	400

int main(int argc, char **argv)
{
char ch[TailleLigne],*word,*id,*phon;
int code,lexid,noid;

noid=False;
if (argc<2)
 {
 fprintf(stderr,"Syntaxe : %s <wordlist> [-noid]\n",argv[0]);
 exit(0);
 }
lexid=load_lexicon(argv[1]);
if ((argc>2)&&(!strcmp(argv[2],"-noid"))) noid=True;

while (fgets(ch,TailleLigne,stdin))
 {
 if (noid)
  {
  word=strtok(ch,"\t\n"); if (word==NULL) ERREUR("bad input file2:",ch);
  phon=strtok(NULL,"\t\n"); if (phon==NULL) ERREUR("bad input file3:",ch);
  if (word2code(lexid,word,&code)) printf("%s\t%s\n",word,phon);
  }
 else
  {
  id=strtok(ch,"\t\n"); if (id==NULL) ERREUR("bad input file1:",ch);
  word=strtok(NULL,"\t\n"); if (word==NULL) ERREUR("bad input file2:",ch);
  phon=strtok(NULL,"\t\n"); if (phon==NULL) ERREUR("bad input file3:",ch);
  if (word2code(lexid,word,&code)) printf("%s\t%s\t%s\n",id,word,phon);
  }
 }
delete_lexicon(lexid);

exit(0);
}
 
