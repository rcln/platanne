/*
 * Put on one line all tokens, after a token starting with XXXX : for
 * from_trs2token
 */
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
 * format intput:
 * 
 * XXXX0 sept heures XXXX1 à l' écoute d' XXXX2 RTM Chaîne Inter XXXX3 . XXXX4 _
 * 
 * format output: XXXX0 sept heures XXXX1 à l' écoute d' XXXX2 RTM Chaîne Inter
 * XXXX3 . XXXX4 _
 */

int 
main(int argc, char **argv)
{
	char            ch[TailleLigne], *pt;
	int             nb, dejaone;

	for (dejaone = False, nb = 0; fgets(ch, TailleLigne, stdin); nb++)
		for (pt = strtok(ch, " \t\n"); pt; pt = strtok(NULL, " \t\n"))
			if (strcmp(pt, "<s>")) {
				if ((!strncmp(pt, "XXXX", 4)) || (!strncmp(pt, "xxxx", 4))) {
					if (dejaone)
						printf("\n");
					else
						dejaone = True;
					printf("%s", pt);
				} else
					printf(" %s", pt);
			}
	exit(0);
}
