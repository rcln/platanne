
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

/* ................................................................ */

#define IF_NUMBER(a)	(((a)>='0')&&((a)<='9'))

int 
if_capital(char c)
{
	if ((c >= 'A') && (c <= 'Z'))
		return True;
	if (
	    (c == 'É') ||
	    (c == 'À') ||
	    (c == 'È') ||
	    (c == 'Ù') ||
	    (c == 'Â') ||
	    (c == 'Ê') ||
	    (c == 'Î') ||
	    (c == 'Ô') ||
	    (c == 'Û') ||
	    (c == 'Ä') ||
	    (c == 'Ë') ||
	    (c == 'Ï') ||
	    (c == 'Ö') ||
	    (c == 'Ü') ||
	    (c == 'Ç')
		)
		return True;
	return False;
}

int 
if_lettre(char c)
{
	if ((c >= 'A') && (c <= 'Z'))
		return True;
	if ((c >= 'a') && (c <= 'z'))
		return True;
	if (
	    (c == 'É') ||
	    (c == 'À') ||
	    (c == 'È') ||
	    (c == 'Ù') ||
	    (c == 'Â') ||
	    (c == 'Ê') ||
	    (c == 'Î') ||
	    (c == 'Ô') ||
	    (c == 'Û') ||
	    (c == 'Ä') ||
	    (c == 'Ë') ||
	    (c == 'Ï') ||
	    (c == 'Ö') ||
	    (c == 'Ü') ||
	    (c == 'Ç') ||
	    (c == 'é') ||
	    (c == 'à') ||
	    (c == 'è') ||
	    (c == 'ù') ||
	    (c == 'â') ||
	    (c == 'ê') ||
	    (c == 'î') ||
	    (c == 'ô') ||
	    (c == 'û') ||
	    (c == 'ä') ||
	    (c == 'ë') ||
	    (c == 'ï') ||
	    (c == 'ö') ||
	    (c == 'ü') ||
	    (c == 'ç')
		)
		return True;
	return False;
}

char 
decapital(char c)
{
	if ((c >= 'A') && (c <= 'Z'))
		return (c + ('a' - 'A'));
	if (c == 'É')
		return 'é';
	if (c == 'À')
		return 'à';
	if (c == 'È')
		return 'è';
	if (c == 'Ù')
		return 'ù';
	if (c == 'Â')
		return 'â';
	if (c == 'Ê')
		return 'ê';
	if (c == 'Î')
		return 'î';
	if (c == 'Ô')
		return 'ô';
	if (c == 'Û')
		return 'û';
	if (c == 'Ä')
		return 'ä';
	if (c == 'Ë')
		return 'ë';
	if (c == 'Ï')
		return 'ï';
	if (c == 'Ö')
		return 'ö';
	if (c == 'Ü')
		return 'ü';
	if (c == 'Ç')
		return 'ç';
	return c;
}

char           *
decapital_string(char *pt)
{
	int             i;
	for (i = 0; pt[i]; i++)
		pt[i] = decapital(pt[i]);
	return pt;
}

int 
at_least_one(char *pt)
{
	int             i;
	for (i = 0; (pt[i]) && ((pt[i] < 'A') || (pt[i] > 'Z')); i++);
	return (pt[i] ? True : False);
}

/* ................................................................ */

int 
if_zarbi(char *pt, int *mixt)
{
	int             zarb;

	for (*mixt = False, zarb = False; *pt; pt++)
		if (((*pt) != '_') && ((*pt) != '\'') && ((*pt) != '\\') && ((*pt) != '/') && ((*pt) != '+') && ((*pt) != '!') &&
		    (!if_lettre(*pt)) && (!(IF_NUMBER((*pt)))))
			zarb = True;
		else
			*mixt = True;
	return zarb;
}

/* ................................................................ */

int 
main(int argc, char **argv)
{
	char            ch[TailleLigne];
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
	for (nb = 0; fgets(ch, TailleLigne, stdin); nb++) {
		printf("%s", decapital_string(ch));
	}
	exit(0);
}
