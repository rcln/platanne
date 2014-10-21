/* Rewrite the output of ne_formtag  */
/* FRED 0109  */

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
 * input: <s> mais lorsque l' on a reconstitué <pers> Georges </pers> maire
 * de <gsp> Vichy </gsp> on a constaté que les sommes en génétique était
 * loin_d' être parfait dans la dernière génération trente pour_cent déjà un
 * n' étaient pas des copies de soude fondateur le renouvellement génétique
 * c' était donc produit </s>
 * 
 * output:
 * 
 * <s>	NONE mais	NONE lorsque	NONE l'	NONE on	NONE a	NONE
 * reconstitué	NONE Georges	pers maire	NONE de	NONE Vichy	gsp
 * on	NONE ....
 */

int 
main(int argc, char **argv)
{
	char            ch[TailleLigne], chtag[100], *pt;
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
	strcpy(chtag, "NONE");
	for (nb = 0; fgets(ch, TailleLigne, stdin); nb++) {
		for (pt = strtok(ch, " \t\n"); pt; pt = strtok(NULL, " \t\n")) {
			if ((pt[0] != '<') || (!strcmp(pt, "<s>")) || (!strcmp(pt, "</s>"))) {
				printf("%s\t%s\n", pt, chtag);
				if (!strncmp(chtag + strlen(chtag) - 2, "_b", 2))
					chtag[strlen(chtag) - 2] = '\0';
			} else {
				if (pt[1] == '/')
					strcpy(chtag, "NONE");
				else {
					strcpy(chtag, pt + 1);
					chtag[strlen(chtag) - 1] = '\0';
					strcat(chtag, "_b");
				}
			}
		}
	}

	exit(0);
}
