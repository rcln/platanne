/* From token°ne files to a STM one  */
/* FRED 0309 - MODIF GERALDINE 0710 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <libxml/parser.h>
#include <libxml/tree.h>

#ifdef LIBXML_TREE_ENABLED

/* ................................................................ */

#define TailleLigne     80000

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
format CTM-NE

20041006_0700_0800_CLASSIQUE 1 1.98 0.19 bonne
20041006_0700_0800_CLASSIQUE 1 2.17 0.54 journée
20041006_0700_0800_CLASSIQUE 1 2.71 0.19 bon
20041006_0700_0800_CLASSIQUE 1 2.90 0.82 courage
20041006_0700_0800_CLASSIQUE 1 3.72 0.27 pour
20041006_0700_0800_CLASSIQUE 1 3.99 0.20 ce
20041006_0700_0800_CLASSIQUE 1 4.19 0.46 mercredi--time--1
20041006_0700_0800_CLASSIQUE 1 4.65 0.25 six--time--1
20041006_0700_0800_CLASSIQUE 1 4.90 0.44 octobre--time--1
20041006_0700_0800_CLASSIQUE 1 5.34 0.31 dans
20041006_0700_0800_CLASSIQUE 1 5.65 0.15 une
20041006_0700_0800_CLASSIQUE 1 5.80 0.33 petite
20041006_0700_0800_CLASSIQUE 1 6.13 0.48 minute
20041006_0700_0800_CLASSIQUE 1 6.61 0.17 le
20041006_0700_0800_CLASSIQUE 1 6.78 0.34 journal
20041006_0700_0800_CLASSIQUE 1 7.12 0.10 de
20041006_0700_0800_CLASSIQUE 1 7.22 0.05 l'
20041006_0700_0800_CLASSIQUE 1 7.27 0.42 économie
20041006_0700_0800_CLASSIQUE 1 7.69 0.42 deuxième
20041006_0700_0800_CLASSIQUE 1 8.11 0.43 édition
20041006_0700_0800_CLASSIQUE 1 8.54 0.10 mais
20041006_0700_0800_CLASSIQUE 1 8.64 0.47 tout_de_suite
20041006_0700_0800_CLASSIQUE 1 9.11 0.17 les
20041006_0700_0800_CLASSIQUE 1 9.28 0.19 grands
20041006_0700_0800_CLASSIQUE 1 9.47 0.31 titres
20041006_0700_0800_CLASSIQUE 1 9.78 0.11 de
20041006_0700_0800_CLASSIQUE 1 9.89 0.05 l'
20041006_0700_0800_CLASSIQUE 1 9.94 0.62 actualité
20041006_0700_0800_CLASSIQUE 1 10.56 0.27 mode
20041006_0700_0800_CLASSIQUE 1 10.83 0.29 bayeux--loc--2
20041006_0700_0800_CLASSIQUE 1 11.12 0.55 bonjour
20041006_0700_0800_CLASSIQUE 1 11.75 0.37 bonjour

format token

<?xml version="1.0" encoding="UTF-8"?>
<Token audio_filename="XXXX" type="MANUAL" asr="NONE" version_date="march2009">
<sentence id="s000001">
<text>le dix huit vingt continue avec le journal de Mickaël Thébault . bonsoir Mickaël . </text>
<tokens count="17">
        <token id="s000001_t0001"  type="sgmltag" content="20070711_1900_1920_inter 1 excluded_region 0.000 81.085 [o,,unknown] ignore_time_segment_in_scorin
g"/>
        <token id="s000001_t0002"  type="sgmltag" content="20070711_1900_1920_inter 1 20070711_1900_1920_inter_speaker_20 81.085 84.582 [o,fx,male]"/>
        <token type="wtoken" id="s000001_t0003">le</token>
        <token type="wtoken" id="s000001_t0004">dix</token>
        <token type="wtoken" id="s000001_t0005">huit</token>
        <token type="wtoken" id="s000001_t0006">vingt</token>
        <token type="wtoken" id="s000001_t0007">continue</token>
        <token type="wtoken" id="s000001_t0008">avec</token>
        <token type="wtoken" id="s000001_t0009">le</token>
        <token type="wtoken" id="s000001_t0010">journal</token>
        <token type="wtoken" id="s000001_t0011">de</token>
        <token type="wtoken" id="s000001_t0012">Mickaël</token>
        <token type="wtoken" id="s000001_t0013">Thébault</token>
        <token type="wtoken" id="s000001_t0014">.</token>
        <token type="wtoken" id="s000001_t0015">bonsoir</token>
        <token type="wtoken" id="s000001_t0016">Mickaël</token>
        <token type="wtoken" id="s000001_t0017">.</token>
</tokens>
</sentence>

format ne

<NE ne_tagger="LIA_NE" type="AUTO" audio_filename="XXXX" version="01" date="????" token_filename="./dev_v3.4/stm_dev_v3.4/20070711_1900_1920_inter.tk2">
<ne id="s000002_ne0001" token="s000002_t0016 s000002_t0017 s000002_t0018 s000002_t0019 s000002_t0020" cat="LOC"> Mickaël Thébault . bonsoir Mickaël </ne>
<ne id="s000005_ne0001" token="s000005_t0016" cat="ORG"> PS </ne>
<ne id="s000006_ne0001" token="s000006_t0007 s000006_t0008" cat="PERS"> Jack Lang </ne>
<ne id="s000007_ne0001" token="s000007_t0011" cat="ORG"> Pierrot </ne>
<ne id="s000007_ne0002" token="s000007_t0015 s000007_t0016" cat="PERS"> Pierre Bodein </ne>
<ne id="s000007_ne0003" token="s000007_t0023 s000007_t0024" cat="AMOUNT"> trente ans </ne>
<ne id="s000008_ne0001" token="s000008_t0012 s000008_t0013 s000008_t0014 s000008_t0015 s000008_t0016" cat="LOC"> cour d' assises du Bas_Rhin </ne>
<ne id="s000009_ne0001" token="s000009_t0011" cat="ORG"> Air_France </ne>
<ne id="s000011_ne0001" token="s000011_t0008" cat="LOC"> Pakistan </ne>

format stm_ne

20030418_0800_0900_FRANCEINTER_DGA 1 Patrick_Roger 12.793 16.698 <o,f3,male> les ministres [pers.hum François Fillon ] et [pers.hum Jean-Paul Delevoye ] dévo

*/

/* ................................................................ */

#define IF_MAJUSCULE(a)	(((a)>='A')&&((a)<='Z'))

typedef struct {
	char           *token;
	char           *cate;
}               type_ne;

#define MAX_NE	10000

type_ne         T_begin_ne[MAX_NE];
type_ne         T_end_ne[MAX_NE];

void 
load_ne(char *chfilene)
{
	FILE           *file;
	char            ch[TailleLigne], chcate[100], *chbegin, *chend;
	int             nbbegin, nbend, i, j;

	if (!(file = fopen(chfilene, "rt")))
		ERREUR("can't open:", chfilene);
	for (nbbegin = nbend = 0; fgets(ch, TailleLigne, file);) {
		if (!strncmp(ch, "<ne ", 4)) {
			chcate[0] = '\0';
			chbegin = chend = NULL;
			for (i = 0; ch[i]; i++)
				if (!strncmp(ch + i, "cat=", 4)) {
					for (j = 0, i += 5; (ch[i]) && (ch[i] != '"'); i++, j++)
						chcate[j] = IF_MAJUSCULE(ch[i]) ? ch[i] + ('a' - 'A') : ch[i];
					if (!ch[i])
						ERREUR("bad format1 token2ctm:", ch);
					chcate[j] = '\0';
				} else if (!strncmp(ch + i, "token=", 6)) {
					chbegin = ch + i + 7;
					for (i += 7; (ch[i]) && (ch[i] != ' ') && (ch[i] != '"'); i++);
					if (!ch[i])
						ERREUR("bad format2:", ch);
					if (ch[i] == '"') {
						chend = chbegin;
						ch[i] = '\0';
					} else {
						ch[i++] = '\0';
						for (chend = ch + i; (ch[i]) && (ch[i] != '"'); i++)
							if (ch[i] == ' ')
								chend = ch + i;
						if (!ch[i])
							ERREUR("bad format3:", ch);
						ch[i] = '\0';
						while ((*chend) && (*chend == ' '))
							chend++;
					}
				}
			if ((!chcate[0]) || (!chbegin) || (!chend))
				ERREUR("bad format4:", ch);
			/*
			 * fprintf(stderr,"chbegin=[%s]
			 * chend=[%s]\n",chbegin,chend);
			 */

			T_begin_ne[nbbegin].token = strdup(chbegin);
			T_begin_ne[nbbegin].cate = strdup(chcate);
			T_end_ne[nbend].token = strdup(chend);
			T_end_ne[nbbegin].cate = strdup(chcate);
			nbbegin++;
			nbend++;
			if ((nbbegin >= MAX_NE) || (nbend >= MAX_NE))
				ERREUR("cste MAX_NE too small", "");
		}
	}
	T_begin_ne[nbbegin].token = T_end_ne[nbend].token = NULL;
	fclose(file);
}

char           *
find_cate(char *id, type_ne * tabl)
{
	int             i;
	for (i = 0; (tabl[i].token) && (strcmp(tabl[i].token, id)); i++)	/* fprintf(stderr,"XX:[%s
										 * ]\n",tabl[i].token) */
		;
	if (tabl[i].token)
		return tabl[i].cate;
	else
		return NULL;
}

/* ................................................................ */

void 
sprint_word(char *ch, xmlNode * node)
{
	if (node) {
		if (node->content) {
			int             i, j;
			char           *chin;
			if ((ch[0]) && (ch[strlen(ch) - 1] != ' '))
				strcat(ch, " ");
			for (i = 0, j = strlen(ch), chin = (char *) node->content; chin[i]; i++) {
				if (chin[i] != '\n')
					if ((i > 0) && (chin[i] == ' ') && (chin[i - 1] == ' '));
					else
						ch[j++] = chin[i];
			}
			ch[j] = '\0';
		}
		sprint_word(ch, node->next);
		sprint_word(ch, node->children);
	}
}

void 
sprint_word_raw(char *ch, xmlNode * node)
{
	if (node) {
		if ((node->content) && (strcmp((char *) node->content, "\n")))
			strcat(ch, (char *) node->content);
		sprint_word(ch, node->next);
		sprint_word(ch, node->children);
	}
}

char           *
find_attribute(xmlAttr * ptat, char *name)
{
	for (; (ptat) && (strcmp((char *) (ptat->name), name)); ptat = ptat->next);
	if ((!ptat) || (ptat->children == NULL) || (ptat->children->content == NULL))
		ERREUR("corpus without ", name);
	return (char *) ptat->children->content;
}

int
if_attribute(xmlAttr * ptat, char *name)
{
	for (; (ptat) && (strcmp((char *) (ptat->name), name)); ptat = ptat->next);
	if ((!ptat) || (ptat->children == NULL) || (ptat->children->content == NULL))
		return 0;
	return 1;
}


xmlNode        *
find_node(xmlNode * a_node, char *name)
{
	xmlNode        *cur_node = NULL, *resu;
	for (cur_node = a_node; cur_node; cur_node = cur_node->next)
		if ((cur_node->type == XML_ELEMENT_NODE) && (!strcmp(cur_node->name, name)))
			return cur_node;
		else {
			resu = find_node(cur_node->children, name);
			if (resu)
				return resu;
		}
	return NULL;
}

xmlNode        *
next_node(xmlNode * pt)
{
	while ((pt) && ((pt->type != XML_ELEMENT_NODE) || (strcmp(pt->name, "token"))))
		pt = pt->next;
	return pt;
}

void 
process_token(xmlNode * a_node, int *nben)
{
	xmlNode        *cur_node = NULL, *pt, *pt2;
	xmlAttr        *ptat;
	static char     ch[TailleLigne], *cate = NULL, *newcate , chendline[TailleLigne];
	static int      prevsgml = False;
	int             i;

	for (cur_node = a_node; cur_node; cur_node = cur_node->next) {
		if (cur_node->type == XML_ELEMENT_NODE) {
			if (!strcmp(cur_node->name, "sentence")) {
				pt = find_node(cur_node->children, "tokens");
				if (!pt)
					ERREUR("bad format in xml: no 'tokens'", "");
				for (pt = pt->children; pt; pt = pt->next)
					if ((pt->type == XML_ELEMENT_NODE) && (!strcmp(pt->name, "token"))) {
						if (!strcmp(find_attribute(pt->properties, "type"), "sgmltag")) {
						        chendline[0] = '\0';
							strcpy(ch, find_attribute(pt->properties, "content"));

							printf("%s", ch);

							if (if_attribute(pt->properties, "endline"))
							        strcpy(chendline, find_attribute(pt->properties, "endline"));
							prevsgml = True;
						} else {
							ch[0] = '\0';
							sprint_word(ch, pt->children);
							newcate = find_cate(find_attribute(pt->properties, "id"), T_begin_ne);
							if (newcate) {
								(*nben)++;
								cate = newcate;
							}
							if (prevsgml)
								printf(" ");
							else 
								printf("_");

							printf("%s", ch);

							pt2 = next_node(pt->next);	/* if (pt2) {
											 * printf("POPO:
											 * type=%s\n",find_attrib
											 * ute(pt2->properties,"t
											 * ype")); } */
							if ((!pt2) || (!strcmp(find_attribute(pt2->properties, "type"), "sgmltag"))) {
							  if (cate) 
							    printf("--%s--%d", cate, *nben);
							  if (chendline[0]) 
							    printf("%s", chendline);
							  printf("\n");
							}
							newcate = find_cate(find_attribute(pt->properties, "id"), T_end_ne);
							if (newcate)
								cate = NULL;

							prevsgml = False;
						}
					}
			}
		}
		process_token(cur_node->children, nben);
	}
}

/* ................................................................ */

int 
main(int argc, char **argv)
{
	char            ch[TailleLigne], *chfilene;
	xmlDoc         *doc = NULL;
	xmlNode        *root_element, *ptnode;
	int             nb;

	/*
	* this initialize the library and check potential ABI mismatches
	* between the version it was compiled for and the actual shared
	* library used.
	*/
	LIBXML_TEST_VERSION
		chfilene = NULL;
	if (argc > 1)
		for (nb = 1; nb < argc; nb++)
			if (!strcmp(argv[nb], "-tk")) {
				if (nb + 1 == argc)
					ERREUR("an option must follow option:", argv[nb]);
				if (!(doc = xmlReadFile(argv[++nb], NULL, 0)))
					ERREUR("could not parse file:", argv[nb]);
			} else if (!strcmp(argv[nb], "-ne")) {
				if (nb + 1 == argc)
					ERREUR("an option must follow option:", argv[nb]);
				chfilene = argv[++nb];
			} else if (!strcmp(argv[nb], "-h")) {
				fprintf(stderr, "Syntax: %s [-h] -tk <file xml> -ne <output>\n", argv[0]);
				exit(0);
			} else
				ERREUR("unknown option:", argv[nb]);

	if ((!doc) || (!chfilene))
		ERREUR("bad syntax, check '-h'", "");

	load_ne(chfilene);

	/* Get the root element node */
	root_element = xmlDocGetRootElement(doc);

	ptnode = find_node(root_element, "Header_CTM");
	ch[0] = '\0';
	sprint_word_raw(ch, ptnode->children);
	if (ch[0] == '\n')
		printf("%s", ch + 1);
	else
		printf("%s", ch);

	ptnode = find_node(root_element, "Token");
	nb = 0;
	process_token(ptnode, &nb);

	/* free the document */
	xmlFreeDoc(doc);
	/*
	*Free the global variables that may
	*have been allocated by the parser.
	*/
	xmlCleanupParser();
	return 0;
}
#else
int 
main(void)
{
	fprintf(stderr, "Tree support not compiled in\n");
	exit(1);
}
#endif
 
