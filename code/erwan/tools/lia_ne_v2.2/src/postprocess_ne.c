/* Postprocess the NE according to simple heuristics  */
/* FRED 1106  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <lia_liblex.h>

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

#define NB_NP	10
char           *T_NP[NB_NP] = {"CHIF", "XFAMIL", "XPAYFP", "XPAYFS", "XPAYMP", "XPAYMS", "XPREF", "XPREM", "XSOC", "XVILLE"};

char           *T_NE[NB_NP] = {"AMOUNT", "PERS", "LOC", "LOC", "LOC", "LOC", "PERS", "PERS", "ORG", "LOC"};

#define IF_DIGIT(a)	(((a)>='0')&&((a)<='9'))
#define IF_CAPITAL(a)	(((a)>='A')&&((a)<='Z'))

char           *T_day[] = {
	"lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi", "dimanche",
	"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche", ""
};

char           *T_month[] = {
	"janvier", "février", "mars", "avril", "mai", "juin", "juillet", "août", "septembre", "octobre", "novembre", "décembre",
	"Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre",
	"fevrier", "Fevrier", "aout", "Aout", "decembre", "Decembre", ""
};

int 
if_day(char *pt)
{
	int             i;
	for (i = 0; (T_day[i][0]) && (strcmp(T_day[i], pt)); i++);
	return (int) (T_day[i][0]);
}

int 
if_month(char *pt)
{
	int             i;
	for (i = 0; (T_month[i][0]) && (strcmp(T_month[i], pt)); i++);
	return (int) (T_month[i][0]);
}

int 
if_number_year(char *pt)
{
	if ((strlen(pt) == 4) && ((!strncmp(pt, "19", 2)) || (!strncmp(pt, "20", 2))) && (IF_DIGIT(pt[2])) && (IF_DIGIT(pt[3])))
		return True;
	else
		return False;
}

int 
if_number_day(char *pt)
{
	if ((pt[1] == '\0') && (IF_DIGIT(pt[0])))
		return True;
	if ((strlen(pt) == 2) && (IF_DIGIT(pt[1])) && (IF_DIGIT(pt[0])) && (pt[0] < '4'))
		return True;
	return False;
}

int 
if_number_hour(char *pt)
{
	if (((pt[0] == '0') || (pt[0] == '1')) && (IF_DIGIT(pt[1])) && (pt[2] == '\0'))
		return True;
	else
		return False;
}

int 
if_number_minute(char *pt)
{
	if ((IF_DIGIT(pt[0])) && (pt[0] <= '6') && (IF_DIGIT(pt[1])) && (pt[2] == '\0'))
		return True;
	else
		return False;
}

int 
at_least_one_cap(char *pt)
{
	int             i;
	for (i = 0; (pt[i]) && (!(IF_CAPITAL(pt[i]))); i++);
	return (int) (pt[i]);
}

/* ................................................................ */

/*
 * format: <s> la Chambre passe à l' étude du projet de loi <pers> C-202
 * </pers> <pers> Loi </pers> modifiant le Code criminel fuite dont le comité
 * a fait rapport avec une proposition d' amendement </s> <s> m. <pers> Dan
 * McTeague </pers> <pers> Pickering--Ajax--Uxbridge </pers> <pers> Lib
 * </pers> propose </s> <s> que le projet de loi soit agréé </s> <s> <prod>
 * La motion est adoptée </prod> </s> <s> m. <pers> Dan McTeague </pers>
 * propose </s> <s> que le projet de loi soit lu pour la troisième fois et
 * adopté </s> <s> m. <pers> Raymond Bonin </pers> <pers> Nickel Belt </pers>
 * <pers> Lib </pers> </s> <s> madame la Présidente j' appuie le projet de
 * loi <pers> C-202 </pers> <pers> Loi </pers> modifiant le Code criminel
 * fuite </s>
 */

#define MAX_SIZE_SENT	180000

int 
main(int argc, char **argv)
{
	char            ch[TailleLigne], *t_sent[MAX_SIZE_SENT], *pt, *prevnetag;
	int             nb, lexid, i, indice, j, ifdone;

	lexid = -1;
	if (argc > 1)
		for (nb = 1; nb < argc; nb++)
			if (!strcmp(argv[nb], "-lex")) {
				if (nb + 1 == argc)
					ERREUR("an option must follow option:", argv[nb]);
				lexid = load_lexicon(argv[++nb]);
			} else if (!strcmp(argv[nb], "-h")) {
				fprintf(stderr, "Syntax: %s [-h] -lex <lex_name.code>\n", argv[0]);
				exit(0);
			} else
				ERREUR("unknown option:", argv[nb]);

	if (lexid == -1)
		ERREUR("bad syntax, check '-h'", "");

	for (nb = 0; fgets(ch, TailleLigne, stdin); nb++) {
		for (pt = strtok(ch, " \t\n"), i = 0; (i < MAX_SIZE_SENT) && (pt); i++, pt = strtok(NULL, " \t\n"))
			t_sent[i] = pt;
		if (i == MAX_SIZE_SENT)
			ERREUR("cste MAX_SIZE_SENT too small:", ch);
		t_sent[i] = NULL;

		for (i = 0; t_sent[i];) {
			if (!strcmp(t_sent[i], "<s>")) {
				printf("<s>");
				i++;
			} else if (!strcmp(t_sent[i], "</s>")) {
				printf(" </s>\n");
				i++;
			} else if (t_sent[i][0] != '<') {	/* outside */
				if (if_day(t_sent[i])) {
					printf(" <TIME> %s", t_sent[i]);
					i++;
					if ((t_sent[i]) && (if_number_day(t_sent[i]))) {
						printf(" %s", t_sent[i]);
						i++;
						if ((t_sent[i]) && (if_month(t_sent[i]))) {
							printf(" %s", t_sent[i]);
							i++;
							if ((t_sent[i]) && (if_number_year(t_sent[i]))) {
								printf(" %s", t_sent[i]);
								i++;
							}
						}
					}
					printf(" </TIME>");
				} else if ((t_sent[i + 1]) && (if_number_day(t_sent[i])) && (if_month(t_sent[i + 1]))) {
					printf(" <TIME> %s %s", t_sent[i], t_sent[i + 1]);
					i += 2;
					if ((t_sent[i]) && (if_number_year(t_sent[i]))) {
						printf(" %s", t_sent[i]);
						i++;
					}
					printf(" </TIME>");
				} else if (if_month(t_sent[i])) {
					printf(" <TIME> %s", t_sent[i]);
					i++;
					if ((t_sent[i]) && (if_number_year(t_sent[i]))) {
						printf(" %s", t_sent[i]);
						i++;
					}
					printf(" </TIME>");
				} else if (if_number_year(t_sent[i])) {
					printf(" <TIME> %s </TIME>", t_sent[i]);
					i++;
				} else if ((if_number_hour(t_sent[i])) && (t_sent[i + 1]) && (!strcmp(t_sent[i + 1], "h")) && (t_sent[i + 2]) && (if_number_minute(t_sent[i + 2]))) {
					printf(" <TIME> %s h %s </TIME>", t_sent[i], t_sent[i + 2]);
					i += 3;
				} else if ((if_number_hour(t_sent[i])) && (t_sent[i + 1]) && (!strcmp(t_sent[i + 1], "h"))) {
					printf(" <TIME> %s h </TIME>", t_sent[i]);
					i += 2;
				} else if (word2code(lexid, t_sent[i], &indice)) {
					if (indice >= NB_NP)
						ERREUR("bad indice:", t_sent[i]);
					prevnetag = T_NE[indice];
					printf(" <%s> %s", prevnetag, t_sent[i]);
					for (++i; (t_sent[i]) && (word2code(lexid, t_sent[i], &indice)) && (!strcmp(T_NE[indice], prevnetag)); i++)
						printf(" %s", t_sent[i]);
					printf(" </%s>", prevnetag);
				} else {	/* default */
					printf(" %s", t_sent[i]);
					i++;
				}

			} else {/* inside */
				ifdone = False;
				if ((!strcmp(t_sent[i], "<PERS>")) || (!strcmp(t_sent[i], "<GSP>")) || (!strcmp(t_sent[i], "<ORG>")) ||
				    (!strcmp(t_sent[i], "<LOC>")) && (!strcmp(t_sent[i], "<FAC>"))) {
					/* if no capital at all, suppress */
					for (j = i + 1; (t_sent[j]) && (t_sent[j][0] != '<') && (!(at_least_one_cap(t_sent[j]))); j++);
					if (t_sent[j] == '\0')
						ERREUR("YUOP::", t_sent[0]);
					if (t_sent[j][0] != '<') {	/* we check for « */
						for (j = i + 1; (t_sent[j]) && (t_sent[j][0] != '<') && (!strstr(t_sent[j], "«")) && (!strstr(t_sent[j], "»")); j++);
						if (t_sent[j][0] == '<')	/* OK */
							j = i + 1;
						else	/* suppress */
							for (; (t_sent[j]) && (t_sent[j][0] != '<'); j++);
					}
					if (t_sent[j][0] == '<') {	/* suppress */
						for (++i; i < j; i++)
							printf(" %s", t_sent[i]);
						i++;
						ifdone = True;
					}
				}
				/* regles ad-hoc */
				if (!ifdone) {
					if ((t_sent[i + 1]) && (!strncmp(t_sent[i + 1], "C-", 2)) && (t_sent[i + 2]) && (t_sent[i + 2][0] == '<')) {
						printf(" <PROD> %s </PROD>", t_sent[i + 1]);
						i += 3;
					} else if ((t_sent[i + 1]) && (t_sent[i + 2]) && (t_sent[i + 2][0] == '<') && (strstr(t_sent[i + 1], "--"))) {
						printf(" <ORG> %s </ORG>", t_sent[i + 1]);
						i += 3;
					} else {	/* default */
						printf(" %s", t_sent[i]);
						for (++i; (t_sent[i]) && (t_sent[i][0] != '<'); i++)
							printf(" %s", t_sent[i]);
						printf(" %s", t_sent[i]);
						i++;
					}
				}
			}
		}

	}

	delete_lexicon(lexid);

	exit(0);
}
