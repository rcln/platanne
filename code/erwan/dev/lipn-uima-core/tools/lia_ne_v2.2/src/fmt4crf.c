
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

char            T_separateur[] = {'(', ')', ',', ';', '!', '0'};

int 
if_separateur(char c)
{
	int             i;
	for (i = 0; (T_separateur[i] != '0') && (T_separateur[i] != c); i++);
	return T_separateur[i] == '0' ? False : True;
}

int 
main(int argc, char **argv)
{
	char            ch[TailleLigne];
	int             nb,debsent;
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
	for (debsent=False,nb = 0; fgets(ch, TailleLigne, stdin); nb++) {
		if (!strncmp(ch, "</s>", 4))
		 {
		 if (debsent) { printf("--LB-- ZTRM\n"); }
		 printf("\n", ch);
		 debsent=False;
		 }
		else
                if (!strncmp(ch, "<s>", 3))
		 debsent=True;
                else
		 {
		 debsent=False;
		 if (if_separateur(ch[0])) printf("\n");
		 printf("%s", ch);
		 }
	}
	exit(0);
} 
