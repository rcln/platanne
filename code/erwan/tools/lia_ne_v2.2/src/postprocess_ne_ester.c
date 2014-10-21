/*
 * Postprocess a NE tagging acording to some rules, specific to ESTER
 * evaluation
 */
/* FRED 0309 - with pattern 0409  */

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
 * format:
 * 
 * STM-NE
 * 
 * 20070716_2045_2100_tvme 1 20070716_2045_2100_tvme_speaker_2 75.193 88.887
 * <o,fx,male> [fonc Sa Majesté le roi ]  [pers Mohammed ]  six réitère
 * ensuite dans s on message sa constante détermination à oeuvrer
 * de_concert_avec le [fonc président français ]  [pers Nicolas Sarkozy ]  ,
 * pour une dynamisation optimale des liens privilégiés de partenariat
 * stratégique
 * 
 * 
 * CTM-NE
 * 
 * 20070716_2045_2100_tvme 1 23.48 0.34  à 20070716_2045_2100_tvme 1 23.81 0.34
 * cette 20070716_2045_2100_tvme 1 24.15 0.34  occasion
 * 20070716_2045_2100_tvme 1 24.48 0.34  Sa--fonc--4 20070716_2045_2100_tvme
 * 1 24.82 0.34  Majesté--fonc--4 20070716_2045_2100_tvme 1 25.16 0.34
 * le--fonc--4 20070716_2045_2100_tvme 1 25.49 0.34  roi--fonc--4
 * 20070716_2045_2100_tvme 1 25.83 0.34  Mohammed--pers--5
 * 20070716_2045_2100_tvme 1 26.16 0.34  six--pers--5 20070716_2045_2100_tvme
 * 1 26.50 0.34  a 20070716_2045_2100_tvme 1 26.83 0.34  adressé
 * 20070716_2045_2100_tvme 1 27.17 0.34  un 20070716_2045_2100_tvme 1 27.51
 * 0.34  message 20070716_2045_2100_tvme 1 27.84 0.34  de
 * 20070716_2045_2100_tvme 1 28.18 0.34  félicitations
 * 20070716_2045_2100_tvme 1 28.51 0.34  [_r_] 20070716_2045_2100_tvme 1
 * 28.85 0.34  au 20070716_2045_2100_tvme 1 29.19 0.34  président--fonc--6
 * 20070716_2045_2100_tvme 1 29.52 0.34  de--fonc--6 20070716_2045_2100_tvme
 * 1 29.86 0.34  la--fonc--6 20070716_2045_2100_tvme 1 30.19 0.34
 * République--fonc--6 20070716_2045_2100_tvme 1 30.53 0.34
 * française--fonc--6 20070716_2045_2100_tvme 1 30.87 0.34  Nicolas--pers--7
 * 20070716_2045_2100_tvme 1 31.20 0.34  Sarkozy--pers--7
 * 
 */

#define CTM	1
#define STM	2

typedef struct {
	char           *info;
	char           *netag;
	char           *nestring;
}               type_hypo;

type_hypo      *T_hypo;
int             NbHypo;

/* ................................................................ */

char          **T_info_ctm;

void 
load_ctm(FILE * file)
{
	static char     ch[TailleLigne], ch2[TailleLigne];
	char           *chname, *chcanal, *chdeb, *chduration, *chword,
	               *chnetag;
	int             nb, i, j, numne, debne, prevnumne;

	for (debne = prevnumne = -1, i = nb = 0; fgets(ch, TailleLigne, file);)
		if ((ch[0] != ';') && (ch[0] != '\n')) {
			strcpy(ch2, ch);
			if (nb >= NbHypo - 100)
				ERREUR("OZOHZJHZH", "");
			chname = chcanal = chdeb = chduration = chword = NULL;
			chname = strtok(ch, " \t\n");
			if (chname)
				chcanal = strtok(NULL, " \t\n");
			if (chcanal)
				chdeb = strtok(NULL, " \t\n");
			if (chdeb)
				chduration = strtok(NULL, " \t\n");
			if (chduration)
				chword = strtok(NULL, " \t\n");
			if ((!chname) || (!chcanal) || (!chdeb) || (!chduration) || (!chword))
				ERREUR("bad format1:", ch2);

			sprintf(ch2, "%s %s %s %s", chname, chcanal, chdeb, chduration);
			T_info_ctm[nb++] = strdup(ch2);
			if (!strstr(chword, "--")) {
				if (prevnumne != -1)
					i++;	/* we were on a NE, we move
						 * forward */
				T_hypo[i].nestring = strdup(chword);
				T_hypo[i].info = T_hypo[i].netag = NULL;
				i++;
				debne = prevnumne = -1;
			} else {
				for (j = 0; (chword[j]) && (strncmp(chword + j, "--", 2)); j++);
				if (!chword[j])
					ERREUR("HJAGJHGJZFGZ", "");
				chword[j] = '\0';
				chnetag = chword + j + 2;
				for (j += 2; (chword[j]) && (strncmp(chword + j, "--", 2)); j++);
				if (!chword[j])
					ERREUR("AFYFZEL:", chnetag);
				chword[j] = '\0';
				if (sscanf(chword + j + 2, "%d", &numne) != 1)
					ERREUR("bad format2:", chword + j + 2);

				if ((prevnumne != -1) && (prevnumne == numne)) {	/* we continue an old
											 * one */
					sprintf(ch2, "%s %s", T_hypo[i].nestring, chword);
					free(T_hypo[i].nestring);
					T_hypo[i].nestring = strdup(ch2);
				} else {	/* we start a new one */
					if (prevnumne != -1)
						i++;	/* we were on a NE, we
							 * move forward */
					T_hypo[i].nestring = strdup(chword);
					T_hypo[i].netag = strdup(chnetag);
					T_hypo[i].info = NULL;
					debne = i;
					prevnumne = numne;
				}
			}
		}
}

void 
print_ctm(FILE * fileout)
{
	int             i, nb, nbne;
	static char     ch[TailleLigne];
	char           *pt;
	for (nb = nbne = i = 0; (T_hypo[i].nestring) || (T_hypo[i].info); i++) {
		if (T_hypo[i].netag) {
			nbne++;
			strcpy(ch, T_hypo[i].nestring);
			for (pt = strtok(ch, " \t\n"); pt; pt = strtok(NULL, " \t\n")) {
				fprintf(fileout, "%s ", T_info_ctm[nb++]);
				fprintf(fileout, "%s--%s--%d\n", pt, T_hypo[i].netag, nbne);
			}
		} else if (strcmp(T_hypo[i].nestring, " "))
			fprintf(fileout, "%s %s\n", T_info_ctm[nb++], T_hypo[i].nestring);
	}
}

/* ................................................................ */

/*
 * 20070716_2045_2100_tvme 1 20070716_2045_2100_tvme_speaker_2 75.193 88.887
 * <o,fx,male> [fonc Sa Majesté le roi ]  [pers Mohammed ]  six réitère
 * ensuite dans
 */

void 
load_stm(FILE * file)
{
	char            ch[TailleLigne], *pt;
	int             nb, i, j;

	for (nb = 0; fgets(ch, TailleLigne, file);)
		if ((ch[0] != ';') && (ch[0] != '\n')) {
			if (nb >= NbHypo - 100)
				ERREUR("OZOHZJHZH", "");
			for (i = 0; (ch[i]) && (ch[i] != '>'); i++);
			if (!ch[i])
				ERREUR("bad format1:", ch);
			if (!ch[i + 1]) {
				T_hypo[nb].nestring = T_hypo[nb].netag = NULL;
				nb++;
			} else {
				ch[i + 1] = '\0';
				T_hypo[nb].info = strdup(ch);
				for (i += 2; ch[i];) {
					for (; (ch[i]) && (ch[i] == ' '); i++);
					if (ch[i] == '[') {	/* NE */
						for (j = i; (ch[j]) && (ch[j] != ' '); j++);
						ch[j] = '\0';
						T_hypo[nb].netag = strdup(ch + i + 1);
						i = j + 1;
						for (++j; (ch[j]) && (ch[j] != ']'); j++);
						if (!ch[j])
							ERREUR("bad format2:", ch + i);
						ch[j] = '\0';
						T_hypo[nb].nestring = strdup(ch + i);
						i = j + 1;
						nb++;
						T_hypo[nb].info = T_hypo[nb].nestring = T_hypo[nb].netag = NULL;
					} else {
						for (j = i; (ch[j]) && (ch[j] != ' '); j++);
						if (ch[j]) {
							ch[j] = '\0';
							T_hypo[nb].nestring = strdup(ch + i);
							i = j + 1;
						} else {
							T_hypo[nb].nestring = strdup(ch + i);
							i = j;
						}
						T_hypo[nb].netag = NULL;
						nb++;
						T_hypo[nb].info = T_hypo[nb].nestring = T_hypo[nb].netag = NULL;
					}
				}
			}
		}
}

void 
print_stm(FILE * fileout)
{
	int             i;
	for (i = 0; (T_hypo[i].nestring) || (T_hypo[i].info); i++) {
		if (T_hypo[i].info)
			fprintf(fileout, "%s", T_hypo[i].info);
		if (T_hypo[i].netag)
			fprintf(fileout, " [%s %s] ", T_hypo[i].netag, T_hypo[i].nestring);
		else
			fprintf(fileout, " %s", T_hypo[i].nestring);
	}
	fprintf(fileout, "\n");
}

/* ................................................................ */

/* Tree NE form with some generalization  */

typedef struct {
	char           *word, *cate;
}               type_gene;

type_gene      *T_gene;

void 
load_gene(FILE * file)
{
	char            ch[TailleLigne], *chword, *chcate;
	int             nb;

	for (nb = 0; fgets(ch, TailleLigne, file); nb++);
	rewind(file);
	T_gene = (type_gene *) malloc(sizeof(type_gene) * (nb + 1));
	for (nb = 0; fgets(ch, TailleLigne, file); nb++) {
		chword = strtok(ch, " \t\n");
		if (!chword)
			ERREUR("bad format:", ch);
		chcate = strtok(NULL, " \t\n");
		if (!chcate)
			ERREUR("bad format:", ch);
		T_gene[nb].word = strdup(chword);
		T_gene[nb].cate = strdup(chcate);
	}
	T_gene[nb].word = T_gene[nb].cate = NULL;
}

char           *
find_gene(char *chword)
{
	int             i;
	for (i = 0; (T_gene[i].word) && (strcmp(T_gene[i].word, chword)); i++);
	return T_gene[i].cate;
}

typedef struct ty_tree {
	char           *word, *cate, *netag;
	struct ty_tree *fg, *fd;
}               type_tree;

type_tree      *Root_neform;

type_tree      *
nouveau_noeud(char *word, char *cate, char *netag)
{
	type_tree      *pt;
	pt = (type_tree *) malloc(sizeof(type_tree));
	if (word)
		pt->word = strdup(word);
	else
		pt->word = NULL;
	if (cate)
		pt->cate = strdup(cate);
	else
		pt->cate = NULL;
	if (netag)
		pt->netag = strdup(netag);
	else
		pt->netag = NULL;
	pt->fg = pt->fd = NULL;
	return pt;
}

#define MAX_SIZE_FORM	1000

type_tree      *
add_ne_form(type_tree * pt, char *t_form[MAX_SIZE_FORM], int *indice, char *netag)
{
	if (pt == NULL)
		pt = nouveau_noeud(t_form[*indice], find_gene(t_form[*indice]), NULL);
	if (!strcmp(pt->word, t_form[*indice])) {
		(*indice)++;
		if (t_form[*indice])
			pt->fg = add_ne_form(pt->fg, t_form, indice, netag);
		else {
			if (pt->netag == NULL)
				pt->netag = strdup(netag);
		};
	} else
		pt->fd = add_ne_form(pt->fd, t_form, indice, netag);
	return pt;
}

type_tree      *
load_neform(FILE * file)
{
	char            ch[TailleLigne], *pt, *netag;
	char           *t_form[MAX_SIZE_FORM];
	int             nb, i, indice;
	type_tree      *racine;

	for (racine = NULL, nb = 0; fgets(ch, TailleLigne, file); nb++) {
		netag = strtok(ch, " \t\n");
		if (!netag)
			ERREUR("bad format1:", ch);
		/*
		 * if
		 * ((!strcmp(netag,"amount"))||(!strcmp(netag,"fonc"))||(!strc
		 * mp(netag,"time")))
		 */
		if ((!strcmp(netag, "fonc")) || (!strcmp(netag, "time"))) {
			for (pt = strtok(NULL, " \t\n"), i = 0; (i < MAX_SIZE_FORM) && (pt); pt = strtok(NULL, " \t\n"), i++)
				t_form[i] = pt;
			if (i == MAX_SIZE_FORM)
				ERREUR("cste MAX_SIZE_FORM too small:", ch);
			if (i > 0) {
				t_form[i] = NULL;
				indice = 0;
				racine = add_ne_form(racine, t_form, &indice, netag);
			}
		}
	}
	return racine;
}

void 
find_longest_match(type_tree * pt, int *indice, int *lindice, char **netag)
{
	char           *chcate;
	if ((pt) && (!T_hypo[*indice].netag) && (T_hypo[*indice].nestring)) {
		chcate = find_gene(T_hypo[*indice].nestring);
		if ((!strcmp(pt->word, T_hypo[*indice].nestring)) || ((pt->cate) && (chcate) && (!strcmp(pt->cate, chcate)))) {
			if (pt->netag) {
				*lindice = (*indice);
				*netag = pt->netag;
			}
			(*indice)++;
			find_longest_match(pt->fg, indice, lindice, netag);
		} else
			find_longest_match(pt->fd, indice, lindice, netag);
	}
}

/* ................................................................ */

/*
 * exemple de regles post FONC + PERS = PERS PERS + FONC = PERS en + TIME =
 * TIME centre international de conférences de LOC = LOC ce TIME soir = TIME
 * stade PERS = LOC quinquennat = AMOUNT commission + ?? = ORG FONC de ORG =
 * PERS
 */

/*
 * now rules stored as patterns: amount # [de] $amount time # [le] $time time
 * # [de] $time time # [du] $time time # [depuis] $time time #  $time [en
 * temps universel]
 */

typedef struct {
	type_hypo      *patt;
	char           *netag;
}               type_pattern;

type_pattern   *T_pattern;

void 
load_pattern(FILE * file)
{
	char            ch[TailleLigne], *pt, ch2[TailleLigne];
	int             nb, i, inword;

	for (nb = 0; fgets(ch, TailleLigne, file); nb++);
	rewind(file);
	T_pattern = (type_pattern *) malloc(sizeof(type_pattern) * (nb + 1));

	for (nb = 0; fgets(ch, TailleLigne, file); nb++) {
		pt = strtok(ch, " ");
		if (!pt)
			ERREUR("bad format1:", ch);
		T_pattern[nb].netag = strdup(pt);
		pt = strtok(NULL, " ");
		if (!pt)
			ERREUR("bad format2:", ch);
		if (strcmp(pt, "#"))
			ERREUR("bad format3:", pt);
		strcpy(ch2, pt + 2);
		for (pt = strtok(NULL, " "), i = 0; pt; pt = strtok(NULL, " "), i++);
		T_pattern[nb].patt = (type_hypo *) malloc(sizeof(type_hypo) * (i + 1));

		for (inword = False, pt = strtok(ch2, " \t\n"), i = 0; pt; pt = strtok(NULL, " \t\n"), i++) {
			T_pattern[nb].patt[i].info = NULL;
			if ((!inword) && (pt[0] != '[')) {
				if (pt[0] != '$')
					ERREUR("don't know what it means, outside word and not a $netag:", pt);
				T_pattern[nb].patt[i].netag = strdup(pt + 1);
				T_pattern[nb].patt[i].nestring = NULL;
			} else {
				if (pt[0] == '[') {
					inword = True;
					T_pattern[nb].patt[i].nestring = strdup(pt + 1);
				} else
					T_pattern[nb].patt[i].nestring = strdup(pt);
				if (pt[strlen(pt) - 1] == ']') {
					inword = False;
					T_pattern[nb].patt[i].nestring[strlen(T_pattern[nb].patt[i].nestring) - 1] = '\0';
				}
				T_pattern[nb].patt[i].netag = NULL;
			}
		}
		T_pattern[nb].patt[i].info = strdup("done");
		T_pattern[nb].patt[i].netag = T_pattern[nb].patt[i].nestring = NULL;
	}
	T_pattern[nb].patt = NULL;
}

void 
use_pattern(int keepinclude)
{
	int             i, j, k, l, foundone, stillok;
	static char     ch[TailleLigne];

	for (i = 0; (T_hypo[i].nestring) || (T_hypo[i].info); i++) {
		for (foundone = False, j = 0; (T_pattern[j].patt) && (!foundone); j++) {
			for (stillok = True, l = 0; (!T_pattern[j].patt[l].info) && (stillok) && ((T_hypo[i + l].nestring) || (T_hypo[i + l].info)); l++) {	/* we check the
																				 * compatibility */
				if ((T_pattern[j].patt[l].netag) && ((!T_hypo[i + l].netag) || (strcmp(T_pattern[j].patt[l].netag, T_hypo[i + l].netag))))
					stillok = False;
				else if ((T_pattern[j].patt[l].nestring) && ((T_hypo[i + l].netag) || (strcmp(T_pattern[j].patt[l].nestring, T_hypo[i + l].nestring))))
					stillok = False;
			}
			if (l == 0)
				ERREUR("pattern vide ??", "");

			if (stillok) {	/* compatible */
				if (keepinclude) {	/* we keep inside NE */
					ERREUR("keepinclude not done yet, sorry ..", "");
				} else {
					if (T_hypo[i].netag)
						free(T_hypo[i].netag);
					T_hypo[i].netag = strdup(T_pattern[j].netag);
					ch[0] = '\0';
					for (k = 0; k < l; k++)
						if (T_hypo[i + k].nestring) {
							if (ch[0])
								strcat(ch, " ");
							strcat(ch, T_hypo[i + k].nestring);
						}
					if (T_hypo[i].nestring)
						free(T_hypo[i].nestring);
					T_hypo[i].nestring = strdup(ch);
					for (k = 1; k < l; k++) {
						if (T_hypo[i + k].netag) {
							free(T_hypo[i + k].netag);
							T_hypo[i + k].netag = NULL;
						}
						if (T_hypo[i + k].nestring) {
							free(T_hypo[i + k].nestring);
							T_hypo[i + k].nestring = strdup(" ");
						}
					}
				}
				foundone = True;
				i += (l - 1);
			}
		}
	}
}

void 
do_rule()
{
	int             i, indice, lindice;
	char           *netag;
	static char     ch[TailleLigne];

	for (i = 0; (T_hypo[i].nestring) || (T_hypo[i].info); i++) {
		/* NE FORM  */
		indice = i;
		lindice = -1;
		netag = NULL;
		find_longest_match(Root_neform, &indice, &lindice, &netag);
		if ((lindice != -1) && (lindice - i >= 1)) {
			if (!netag)
				ERREUR("AALKJZLKJZLKJZ", "");
			if (0) {/* verbose */
				fprintf(stderr, "TEST: %s =>", netag);
				for (indice = i; indice <= lindice; indice++)
					fprintf(stderr, " %s", T_hypo[indice].nestring);
				fprintf(stderr, "\n");
			}
			ch[0] = '\0';
			for (indice = i; indice <= lindice; indice++) {
				strcat(ch, " ");
				strcat(ch, T_hypo[indice].nestring);
				free(T_hypo[indice].nestring);
				T_hypo[indice].nestring = strdup(" ");
			}
			free(T_hypo[i].nestring);
			T_hypo[i].nestring = strdup(ch);
			T_hypo[i].netag = strdup(netag);
			i = lindice;
		}
	}
}

/* ................................................................ */

int 
main(int argc, char **argv)
{
	char            ch[TailleLigne], *pt;
	int             nb, typehypo;
	FILE           *filehypo, *filegene, *fileform, *filepatt;

	typehypo = 0;
	filehypo = filegene = NULL;
	if (argc > 1)
		for (nb = 1; nb < argc; nb++)
			if (!strcmp(argv[nb], "-file")) {
				if (nb + 1 == argc)
					ERREUR("an option must follow option:", argv[nb]);
				if (!(filehypo = fopen(argv[++nb], "rt")))
					ERREUR("can't read:", argv[nb]);
			} else if (!strcmp(argv[nb], "-pattern")) {
				if (nb + 1 == argc)
					ERREUR("an option must follow option:", argv[nb]);
				if (!(filepatt = fopen(argv[++nb], "rt")))
					ERREUR("can't read:", argv[nb]);
			} else if (!strcmp(argv[nb], "-form")) {
				if (nb + 1 == argc)
					ERREUR("an option must follow option:", argv[nb]);
				if (!(fileform = fopen(argv[++nb], "rt")))
					ERREUR("can't read:", argv[nb]);
			} else if (!strcmp(argv[nb], "-gene")) {
				if (nb + 1 == argc)
					ERREUR("an option must follow option:", argv[nb]);
				if (!(filegene = fopen(argv[++nb], "rt")))
					ERREUR("can't read:", argv[nb]);
			} else if (!strcmp(argv[nb], "-ctm"))
				typehypo = CTM;
			else if (!strcmp(argv[nb], "-stm"))
				typehypo = STM;
			else if (!strcmp(argv[nb], "-h")) {
				fprintf(stderr, "Syntax: %s [-h] -file <file> -type <stm/ctm> -gene <file> -form <file neform> -pattern <file>\n", argv[0]);
				exit(0);
			} else
				ERREUR("unknown option:", argv[nb]);

	if ((!typehypo) || (!filehypo) || (!filegene) || (!fileform) || (!filepatt))
		ERREUR("bad syntax, check '-h'", "");

	load_gene(filegene);
	fclose(filegene);
	Root_neform = load_neform(fileform);
	fclose(fileform);
	load_pattern(filepatt);
	fclose(filepatt);
	for (nb = 0; fgets(ch, TailleLigne, filehypo);)
		for (pt = strtok(ch, " \t\n"); pt; pt = strtok(NULL, " \t\n"))
			nb++;
	NbHypo = nb + 100;
	T_hypo = (type_hypo *) malloc(sizeof(type_hypo) * NbHypo);
	if (typehypo == CTM)
		T_info_ctm = (char **) malloc(sizeof(char *) * NbHypo);
	rewind(filehypo);
	if (typehypo == STM)
		load_stm(filehypo);
	else
		load_ctm(filehypo);
	fclose(filehypo);
	use_pattern(False);
	do_rule();
	if (typehypo == STM)
		print_stm(stdout);
	else
		print_ctm(stdout);
	exit(0);
}
