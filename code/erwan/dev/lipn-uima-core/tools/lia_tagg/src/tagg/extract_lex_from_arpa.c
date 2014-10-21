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
/*  Extract a sirlex lexicon from a arpa lm file
    (=liste of unigram)  */
/*  FRED 1101  - modif 1109 begin when /data found !!!!  -  patch Rosa for '\' sept2010  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>

/*................................................................*/

#define TailleLigne	10000

#define True	1
#define False	0

#define max(a,b)        ((a)>(b)?(a):(b))
#define min(a,b)        ((a)<(b)?(a):(b))

void ERREUR(char *ch1,char *ch2)
{
fprintf(stderr,"ERREUR : %s %s\n",ch1,ch2);
exit(0);
}

/*................................................................*/

int main(int argc, char **argv)
{
char ch[TailleLigne],*pb,*word;
int finish,ok,nb;
for(finish=False,ok=nb=0;(finish==False)&&(fgets(ch,TailleLigne,stdin));)
 {
/*
 DEBUG DEBUG
 if ((!strncmp(ch+1,"data",4))||(!strncmp(ch+2,"data",4))) { if (!strncmp(ch,"\\data",5)) fprintf(stderr,"ZARB1:[%s]\n",ch); }
 if ((!strncmp(ch+1,"1-grams:",8))||(!strncmp(ch+2,"1-grams:",8))) { if (!strncmp(ch,"\\1-grams:",9)) fprintf(stderr,"ZARB2:[%s]\n",ch); }
*/

/* OLD doesn't work depending of the encoding of the '\' (Rosa sept2010)
 if (!strncmp(ch,"\\data",5)) ok=1;
 else
 if (!strncmp(ch,"\\1-grams:",9)) { if (ok==1) ok=2; }
*/
 if ((!strncmp(ch+1,"data",4))||(!strncmp(ch+2,"data",4))) ok=1;
 else
 if ((!strncmp(ch+1,"1-grams:",8))||(!strncmp(ch+2,"1-grams:",8))) { if (ok==1) ok=2; }
 else
  if (ok==2)
   if (ch[0]=='\n') finish=True;
   else
    {
    pb=strtok(ch," \t\n"); if (pb==NULL) ERREUR("bad format in input file","");
    word=strtok(NULL," \t\n"); if (word==NULL) ERREUR("bad format in input file","");
    printf("%d\t%s\n",nb++,word);
    }
 }
return 0;
}
 
