/*
 * From a TOKEN file with POS file to a ECG (word+pos) file. For the
 * potential proper name (words starting with a capital and a UNK, MOTINC or
 * X?? tag, it's replaced by the NE tag if it belongs to LOC, ORG, PERS, PROD
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

#define SI_MINUSCULE(a)	(((a)>='a')&&((a)<='z'))

/* ................................................................ */

/*
 * Format:
 * 
 * file .tk
 * 
 * <sentence id="s0001"> <text>merci ï¿½ tous d' ï¿½couter France_Inter euh vous
 * ï¿½tes en ce moment un million deux cent mille ï¿½ ï¿½tre branchï¿½s sur
 * l' antenne de France_Inter , et_puis ce qui nous fait plaisir aussi , c'
 * est que le petit matin marche trï¿½s_bien . </text> <tokens count="93">
 * <token id="s0001_t0001" type="sgmltag"><Sync time="0"/></token> <token
 * type="space"  id="s0001_t0002"/> <token type="wtoken"
 * id="s0001_t0003">merci</token> <token type="space"  id="s0001_t0004"/>
 * <token type="wtoken" id="s0001_t0005">ï¿½</token> <token type="space"
 * id="s0001_t0006"/> <token type="wtoken" id="s0001_t0007">tous</token>
 * 
 * file .pos
 * 
 * <word id="s0001_w0001" token="s0001_t0003" pos="NMS"> merci </word> <word
 * id="s0001_w0002" token="s0001_t0005" pos="PREPADE"> ï¿½ </word> <word
 * id="s0001_w0003" token="s0001_t0007" pos="AINDMP"> tous </word> <word
 * id="s0001_w0004" token="s0001_t0009" pos="PREPADE"> d' </word> <word
 * id="s0001_w0005" token="s0001_t0011" pos="VINF"> ï¿½couter </word> <word
 * id="s0001_w0006" token="s0001_t0014" pos="XSOC"> France_Inter </word>
 * <word id="s0001_w0007" token="s0001_t0017" pos="ADV"> euh </word> <word
 * id="s0001_w0008" token="s0001_t0019" pos="PPER2P"> vous </word> <word
 * id="s0001_w0009" token="s0001_t0021" pos="VE2P"> ï¿½tes </word> <word
 * id="s0001_w0010" token="s0001_t0023" pos="PREP"> en </word> <word
 * id="s0001_w0011" token="s0001_t0025" pos="DETMS"> ce </word>
 * 
 */

void 
analyze_pos(char *ch, char **chid, char **chpos, char **chword)
{
	int             i;
	for (i = 0; (ch[i]) && (strncmp(ch + i, "token=", 6)); i++);
	if (!ch[i])
		ERREUR("bad POS string:", ch);
	(*chid) = ch + i + 7;
	for (i += 7; (ch[i]) && (ch[i] != '"'); i++);
	if (!ch[i])
		ERREUR("bad POS string:", ch);
	ch[i] = '\0';
	for (++i; (ch[i]) && (strncmp(ch + i, "pos=", 4)); i++);
	if (!ch[i])
		ERREUR("bad POS string:", ch);
	(*chpos) = ch + i + 5;
	for (i += 5; (ch[i]) && (ch[i] != '"'); i++);
	if (!ch[i])
		ERREUR("bad POS string:", ch);
	ch[i] = '\0';
	for (++i; (ch[i]) && (ch[i] != '>'); i++);
	if (!ch[i])
		ERREUR("bad POS string:", ch);
	for (++i; (ch[i]) && (ch[i] == ' '); i++);
	(*chword) = ch + i;
	for (++i; (ch[i]) && (ch[i] != ' ') && (ch[i] != '<'); i++);
	if (!ch[i])
		ERREUR("bad POS string:", ch);
	ch[i] = '\0';
}

/*
 * analyse NE <token id="s0001_t0003" type="sgmltag"><Event desc="time.hour"
 * type="entities" extent="begin"/></token>
 */

void 
analyze_tag(char *ch, char **desc, char **type, char **extent)
{
	int             i;
	*desc = *type = *extent = NULL;
	for (i = 0; ch[i]; i++)
		if (!strncmp(ch + i, "desc=", 5)) {
			*desc = ch + i + 6;
			for (i += 6; (ch[i]) && (ch[i] != '"'); i++);
			if (!ch[i])
				ERREUR("bad format:", ch);
			ch[i] = '\0';
		} else if (!strncmp(ch + i, "type=", 5)) {
			*type = ch + i + 6;
			for (i += 6; (ch[i]) && (ch[i] != '"'); i++);
			if (!ch[i])
				ERREUR("bad format:", ch);
			ch[i] = '\0';
		} else if (!strncmp(ch + i, "extent=", 7)) {
			*extent = ch + i + 8;
			for (i += 8; (ch[i]) && (ch[i] != '"'); i++);
			if (!ch[i])
				ERREUR("bad format:", ch);
			ch[i] = '\0';
		}
}

int 
main(int argc, char **argv)
{
	char            ch[TailleLigne], ch2[TailleLigne], *chid, *chpos,
	               *chword, *chdesc, *chtype, *chextent, chNE[100];
	int             nb, insent, i, j;
	FILE           *file_tk, *file_pos;

	file_tk = file_pos = NULL;
	if (argc > 1)
		for (nb = 1; nb < argc; nb++)
			if (!strcmp(argv[nb], "-tk")) {
				if (nb + 1 == argc)
					ERREUR("an option must follow option:", argv[nb]);
				if (!(file_tk = fopen(argv[++nb], "rt")))
					ERREUR("can't open:", argv[nb]);
			} else if (!strcmp(argv[nb], "-pos")) {
				if (nb + 1 == argc)
					ERREUR("an option must follow option:", argv[nb]);
				if (!(file_pos = fopen(argv[++nb], "rt")))
					ERREUR("can't open:", argv[nb]);
			} else if (!strcmp(argv[nb], "-h")) {
				fprintf(stderr, "Syntax: %s [-h] -tk <file> -pos <file>\n", argv[0]);
				exit(0);
			} else
				ERREUR("unknown option:", argv[nb]);

	if ((!file_tk) || (!file_pos))
		ERREUR("bad syntax, check '-h'", "");

	while ((fgets(ch2, TailleLigne, file_pos)) && (!strstr(ch2, "<word")));
	if (!feof(file_pos))
		analyze_pos(ch2, &chid, &chpos, &chword);
	else
		ERREUR("empty POS file ??", "");
	for (chNE[0] = '\0', insent = False, nb = 0; fgets(ch, TailleLigne, file_tk); nb++)
		if (strstr(ch, "<tokens")) {
			insent = True;
			printf("<s> ZTRM\n");
		} else if (strstr(ch, "</tokens")) {
			insent = False;
			printf("</s> ZTRM\n");
		} else if (insent) {
			if (strstr(ch, "<token "))
				if (!strstr(ch, "sgmltag")) {
					if (chid == NULL)
						ERREUR("files TOKEN and POS are not synchronized !!", "");
					for (i = 0; (ch[i]) && (strncmp(ch + i, "id=", 3)); i++);
					if (!ch[i])
						ERREUR("bad token string:", ch);
					for (j = i + 4; (ch[j]) && (ch[j] != '"'); j++);
					if (!ch[j])
						ERREUR("bad token string:", ch);
					ch[j] = '\0';
					/*
					 * printf("[%s] ==
					 * [%s]\n",ch+i+4,chid) ;
					 */
					if (!strcmp(ch + i + 4, chid)) {
						printf("%s ", chword);
						if ((if_capital(chword[0])) && (chNE[0]) && ((!strcmp(chNE, "org")) || (!strcmp(chNE, "pers")) || (!strcmp(chNE, "prod")) || (!strcmp(chNE, "loc"))) &&
						    ((!strcmp(chpos, "MOTINC")) || (chpos[0] == 'X') || (!strcmp(chpos, "UNK")))) {
							printf("X");
							for (j = 0; chNE[j]; j++)
								if (SI_MINUSCULE(chNE[j]))
									printf("%c", SI_MINUSCULE(chNE[j]) ? chNE[j] - ('a' - 'A') : chNE[j]);
							printf("\n");
						} else
							printf("%s\n", chpos);
						while ((fgets(ch2, TailleLigne, file_pos)) && (!strstr(ch2, "<word")));
						if (!feof(file_pos))
							analyze_pos(ch2, &chid, &chpos, &chword);
						else
							chid = chpos = chword = NULL;
					}
				} else {
					analyze_tag(ch, &chdesc, &chtype, &chextent);
					if (!strcmp(chtype, "entities")) {
						if (!strcmp(chextent, "begin")) {
							for (i = 0; (chdesc[i]) && (chdesc[i] != '.'); i++)
								chNE[i] = chdesc[i];
							chNE[i] = '\0';
						} else
							chNE[0] = '\0';
					}
				}
		}
	exit(0);
}
