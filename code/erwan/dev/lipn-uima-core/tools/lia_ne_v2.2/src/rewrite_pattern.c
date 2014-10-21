/* Rewrite pattern for postprocess_ne_ester  */
/* FRED 0409  */

/*
 * from:
 * 
 * amount # [de] * time # [le] * time # [de] * time # [du] * time # [depuis] *
 * 
 * to
 * amount # [de] $amount time # [le] $time time # [de] $time time # [du] $time
 * time # [depuis] $time
 * 
 */

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

int 
main(int argc, char **argv)
{
	char            ch[TailleLigne], *pt;
	int             nb, i;
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

	for (nb = 0; fgets(ch, TailleLigne, stdin); nb++) {
		pt = strtok(ch, " ");
		if (pt)
			pt = strtok(NULL, "\n");
		if (!pt)
			ERREUR("bad format:", ch);
		printf("%s ", ch);
		for (i = 0; pt[i]; i++)
			if (pt[i] == '*')
				printf("$%s", ch);
			else
				printf("%c", pt[i]);
		printf("\n");
	}

	exit(0);
}
