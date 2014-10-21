/* Extract the NE form from a trs file  */
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
<Event desc="org.div" type="entities" extent="begin"/>
France Inter
<Event desc="org.div" type="entities" extent="end"/>
*/

int 
main(int argc, char **argv)
{
	char            ch[TailleLigne], chne[100], ch2[TailleLigne], *pt;
	int             nb, i, j;
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
		if ((!strncmp(ch, "<Event", 6)) && (strstr(ch, "entities")) && (strstr(ch, "begin"))) {
			for (i = 0; (ch[i]) && (strncmp(ch + i, "desc=", 5)); i++);
			if (!ch[i])
				ERREUR("bad format:", ch);
			for (i += 6, j = 0; (ch[i]) && (ch[i] != '"') && (ch[i] != '.'); i++, j++)
				chne[j] = ch[i];
			if (!ch[i])
				ERREUR("bad format:", ch);
			chne[j] = '\0';
			printf("NE#%s\t", chne);
			ch2[0] = '\0';
			while ((fgets(ch, TailleLigne, stdin)) && ((strncmp(ch, "<Event", 6)) || (!strstr(ch, "entities")) || (!strstr(ch, "end")) || (!strstr(ch, chne))))
				/* if (strncmp(ch,"<Event",6)) */
				if (ch[0] != '<') {
					strtok(ch, "\n");
					strcat(ch2, ch);
					strcat(ch2, " ");
					if (strlen(ch2) > TailleLigne - 100)
						ERREUR("ZARB:", ch2);
				}
			for (pt = strtok(ch2, " \t\n"); pt; pt = strtok(NULL, " \t\n"))
				printf("%s ", pt);
			printf("\n");
		}
	}

	exit(0);
}
