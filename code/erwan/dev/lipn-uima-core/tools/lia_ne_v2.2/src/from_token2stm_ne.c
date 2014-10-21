/* From token°ne files to a STM one  */
/* FRED 0309  */

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
format STM

;; Transcriber export by stm.tcl,v 1.21 on ven mar 06 11:37:51 CET 2009 with encoding ISO-8859-1
;; transcribed by (null), version 35 of 090306
;;
;; CATEGORY "0" "" ""
;; LABEL "O" "Overall" "Overall"
;;
;; CATEGORY "1" "Hub4 Focus Conditions" ""
;; LABEL "F0" "Baseline//Broadcast//Speech" ""
;; LABEL "F1" "Spontaneous//Broadcast//Speech" ""
;; LABEL "F2" "Speech Over//Telephone//Channels" ""
;; LABEL "F3" "Speech in the//Presence of//Background Music" ""
;; LABEL "F4" "Speech Under//Degraded//Acoustic Conditions" ""
;; LABEL "F5" "Speech from//Non-Native//Speakers" ""
;; LABEL "FX" "All other speech" ""
;; CATEGORY "2" "Speaker Sex" ""
;; LABEL "female" "Female" ""
;; LABEL "male"   "Male" ""
;; LABEL "unknown"   "Unknown" ""
20070711_1900_1920_inter 1 excluded_region 0.000 81.085 <o,,unknown> ignore_time_segment_in_scoring
20070711_1900_1920_inter 1 20070711_1900_1920_inter_speaker_20 81.085 84.582 <o,fx,male> le 18 20 continue avec le journal de Mickaël Thébault . bonsoir Mick
aël .
20070711_1900_1920_inter 1 Mickaël_Thébault 84.582 85.476 <o,f0,male> bonsoir .
20070711_1900_1920_inter 1 inter_segment_gap 85.476 85.982 <o,f3,>
20070711_1900_1920_inter 1 Mickaël_Thébault 85.982 91.647 <o,f3,male> quand l'ouverture sarkozyenne fait des ravages : au PS encore un éléphant quitte le tro
upeau .
20070711_1900_1920_inter 1 Mickaël_Thébault 91.647 96.691 <o,f3,male> Jack Lang démissionne des instances dirigeantes du parti , réactions à gauche et à droi
te dans un instant .
20070711_1900_1920_inter 1 Mickaël_Thébault 96.691 104.302 <o,f3,male> [r] la peine maximale pour Pierrot le fou , Pierre Bodein condamné à la réclusion crim
inelle à perpétuité dont 30 ans incompressibles .


format token

<?xml version="1.0" encoding="UTF-8"?>
<Token audio_filename="XXXX" type="MANUAL" asr="NONE" version_date="march2009">
<Header_STM>
;; Transcriber export by stm.tcl,v 1.21 on ven mar 06 11:37:51 CET 2009 with encoding ISO-8859-1
;; transcribed by (null), version 35 of 090306
;;
;; CATEGORY "0" "" ""
;; LABEL "O" "Overall" "Overall"
;;
;; CATEGORY "1" "Hub4 Focus Conditions" ""
;; LABEL "F0" "Baseline//Broadcast//Speech" ""
;; LABEL "F1" "Spontaneous//Broadcast//Speech" ""
;; LABEL "F2" "Speech Over//Telephone//Channels" ""
;; LABEL "F3" "Speech in the//Presence of//Background Music" ""
;; LABEL "F4" "Speech Under//Degraded//Acoustic Conditions" ""
;; LABEL "F5" "Speech from//Non-Native//Speakers" ""
;; LABEL "FX" "All other speech" ""
;; CATEGORY "2" "Speaker Sex" ""
;; LABEL "female" "Female" ""
;; LABEL "male"   "Male" ""
;; LABEL "unknown"   "Unknown" ""
</Header_STM>
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
						ERREUR("bad format1:", ch);
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
		if (node->content)
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

xmlNode        *
find_node(xmlNode * a_node, char *name)
{
	xmlNode        *cur_node = NULL, *resu;
	for (cur_node = a_node; cur_node; cur_node = cur_node->next)
		if ((cur_node->type == XML_ELEMENT_NODE) && (!strcmp((char *)(cur_node->name), name)))
			return cur_node;
		else {
			resu = find_node(cur_node->children, name);
			if (resu)
				return resu;
		}
	return NULL;
}

void 
process_token(xmlNode * a_node)
{
	xmlNode        *cur_node = NULL, *pt, *pt2;
	xmlAttr        *ptat;
	static char     ch[TailleLigne], *cate, *newcate;
	static int      interuptus, prevsgml;
	int             i;

	for (interuptus = prevsgml = False, cate = NULL, cur_node = a_node; cur_node; cur_node = cur_node->next) {
		if (cur_node->type == XML_ELEMENT_NODE) {
			if (!strcmp((char *)(cur_node->name), "sentence")) {
				pt = find_node(cur_node->children, "tokens");
				if (!pt)
					ERREUR("bad format in xml: no 'tokens'", "");
				for (pt = pt->children; pt; pt = pt->next)
					if ((pt->type == XML_ELEMENT_NODE) && (!strcmp((char *)(pt->name), "token"))) {
						if (!strcmp(find_attribute(pt->properties, "type"), "sgmltag")) {
							if (cate) {
								if (!prevsgml) {
									printf(" ] ");
									interuptus = True;
								}
							} else
								interuptus = False;
							strcpy(ch, find_attribute(pt->properties, "content"));
							for (i = 0; ch[i]; i++)
								if (ch[i] == '[')
									ch[i] = '<';
								else if (ch[i] == ']')
									ch[i] = '>';
							printf("\n%s", ch);
							prevsgml = True;
						} else {
							ch[0] = '\0';
							sprint_word(ch, pt->children);
							newcate = find_cate(find_attribute(pt->properties, "id"), T_begin_ne);
							if (newcate) {
								printf(" [%s", newcate);
								cate = newcate;
							} else {
								if ((cate) && (interuptus)) {
									printf(" [%s", cate);
									interuptus = False;
								}
							}
							printf(" %s", ch);
							newcate = find_cate(find_attribute(pt->properties, "id"), T_end_ne);
							if (newcate) {
								printf(" ] ");
								cate = NULL;
								interuptus = False;
							}
							prevsgml = False;
						}
					}
			}
		}
		process_token(cur_node->children);
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
				fprintf(stderr, "Syntax: %s [-h] -doc <file xml> -ne <output>\n", argv[0]);
				exit(0);
			} else
				ERREUR("unknown option:", argv[nb]);

	if ((!doc) || (!chfilene))
		ERREUR("bad syntax, check '-h'", "");

	load_ne(chfilene);

	/* Get the root element node */
	root_element = xmlDocGetRootElement(doc);

	ptnode = find_node(root_element, "Header_STM");
	ch[0] = '\0';
	sprint_word_raw(ch, ptnode->children);
	printf("%s", ch);

	ptnode = find_node(root_element, "Token");
	process_token(ptnode);

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
