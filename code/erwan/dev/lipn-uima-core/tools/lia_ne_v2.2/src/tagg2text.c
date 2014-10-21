/*
 * Output a text corpus with  NE tags from a tagged corpus with some
 * rewritting rules
 */
/* FRED1106  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>

/* ................................................................ */

#define TailleLigne     180000

#define True    1
#define False   0

void 
ERREUR(char *ch1, char *ch2)
{
	fprintf(stderr, "ERREUR : %s %s\n", ch1, ch2);
	exit(0);
}

/* ................................................................ */

#define IFCAPITAL(a)	(((a)>='A')&&((a)<='Z'))

char            T_separateur[] = {'(', ')', ',', ';', '!', ':', '0'};

int 
if_separateur(char c)
{
	int             i;
	for (i = 0; (T_separateur[i] != '0') && (T_separateur[i] != c); i++);
	return T_separateur[i] == '0' ? False : True;
}


/*
 * format:
 * 
 * de      PREPADE O la      DETFS   O Loi     MOTINC  O sur     PREP    O le
 * DETMS   O Parlement       MOTINC  B-loc du      PREPDU  I-loc Canada
 * XPAYMS  I-loc
 * 
 */

int 
potential_cpn(char *ch)
{
	if ((IFCAPITAL(ch[0])) && (strstr(ch, "--")))
		return True;
	return False;
}

int 
main(int argc, char **argv)
{
	char            ch[TailleLigne], prev[TailleLigne], *pt1, *pt2,
	               *pt3;
	int             nb;

	/*
	if (argc>1)
	 for(nb=1;nb<argc;nb++)
	  if (!strcmp(argv[nb],"-XXXX"))
	   {
	   if (nb+1==argc) ERREUR("an option must follow option:",argv[nb]);
	   XXXX
	   }
	  else
	  if (!strcmp(argv[nb],"-h"))
	   {
	   fprintf(stderr,"Syntax: %s [-h]\n",argv[0]);
	   exit(0);
	   }
	  else ERREUR("unknown option:",argv[nb]);
	*/

	prev[0] = '\0';

	printf("<s> ");
	fgets(ch, TailleLigne, stdin);
	for (nb = 0; !feof(stdin); nb++)
         {
	 if ((nb + 1) % 100000 == 0) fprintf(stderr, "En cours : %d\n", nb + 1);
	 if ((ch[0] == '\0') || (ch[0] == '\n') || (!strncmp(ch,"--LB--",6)))
	  {
	  if (prev[0]) printf("</%s> ", prev);
          /*before if (!strncmp(pt1,"--LB--",6)) { printf("</s>\n<s> --LB-- </s>\n<s> "); fgets(ch, TailleLigne, stdin); }*/
          if (!strncmp(ch,"--LB--",6)) { printf("</s>\n<s> --LB-- </s>\n<s> "); fgets(ch, TailleLigne, stdin); }
          else if ((fgets(ch, TailleLigne, stdin)) && (!if_separateur(ch[0]))) printf("</s>\n<s> ");
          prev[0] = '\0';
	  }
         else
          {
	  pt1 = pt2 = pt3 = NULL;
	  pt1 = strtok(ch, " \t\n");
	  if (pt1) pt2 = strtok(NULL, " \t\n");
	  if (pt2) pt3 = strtok(NULL, " \t\n");
 	  if ((!pt1) || (!pt2) || (!pt3)) ERREUR("bad format:", ch);
  	  if (pt3[0] == 'O')
           {
	   if (prev[0]) printf("</%s> ", prev);
	   prev[0] = '\0';
	   /* correction: for the PN compound with '--' if (potential_cpn(pt1)) { strcpy(prev, "gsp"); printf("<%s> ", prev); } */
           }
          else
           {
	   if (strcmp(prev, pt3 + 2))
            {
	    if (prev[0]) printf("</%s> ", prev);
	    strcpy(prev, pt3 + 2);
	    printf("<%s> ", prev);
	    }
	   }
	  if (!strcmp(pt1, "</s>")) printf("</s>\n");
  	  else printf("%s ", pt1);
	  fgets(ch, TailleLigne, stdin);
	  }
	}
	printf("</s>\n");

	exit(0);
}
 
