/* Clean the output of the script get_ne_form.csh  */
/* FRED 0309  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>

/* ................................................................ */

#define TailleLigne     8000

#define True    1
#define False   0

void 
ERREUR(char *ch1, char *ch2)
{
	fprintf(stderr, "ERREUR : %s %s\n", ch1, ch2);
	exit(0);
}

/* ................................................................ */

/*
NE#fonc
lieutenant
de
la
Marine
NE#org
Portugal
NE#org
Union
européenne
*/

int 
main(int argc, char **argv)
{
	char            ch[TailleLigne], *pt;
	int             nb, firstone;
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
	for (firstone = True, nb = 0; fgets(ch, TailleLigne, stdin); nb++)
		for (pt = strtok(ch, " \t\n"); pt; pt = strtok(NULL, " \t\n"))
			if ((strcmp(pt, "<s>")) && (strcmp(pt, "</s>"))) {
				if (!strncmp(pt, "NE#", 3)) {
					if (!firstone)
						printf("\n");
					else
						firstone = False;
					printf("%s\t", pt + 3);
				} else
					printf("%s ", pt);
			}
	printf("\n");

	exit(0);
}
