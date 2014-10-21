/*
 * Take as input a Transcriber .trs file and add <sentence> tags with token
 * segmentation for each turn, following the MACAON token format. Prior to
 * that, each sentence is tokenized thanks to Biglex
 */
/* FRED 1007  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <unistd.h>
#include <sys/times.h>

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
 * Format TRS <Turn speaker="spk147" startTime="0" endTime="2.22"> <Sync
 * time="0"/> aï¿½e , merci <Event desc="pers.hum" type="entities"
 * extent="begin"/> Joï¿½l Collado <Event desc="pers.hum" type="entities"
 * extent="end"/> . <Event desc="org.div" type="entities" extent="begin"/>
 * France Inter <Event desc="org.div" type="entities" extent="end"/> , il est
 * <Event desc="time.hour" type="entities" extent="begin"/> 8 heures <Event
 * desc="time.hour" type="entities" extent="end"/> . </Turn>
 */

/*
 * Format TOKEN <sentence id="s00005"> <text><sil> <sil> et ce(2) qui fÃ»t
 * bon j' en(2) mange les hasards d' un(2) ange(2) de(2) [carillon]</text>
 * <tokens count="17"> <token type="sgmltag" id="s00005_t0001"><Sync
 * time="50.00" /></token> <token type="silence"
 * id="s00005_t0002"><sil></token> <token type="wtoken"
 * id="s00005_t0003">et</token> <token type="wtoken"
 * id="s00005_t0004">ce</token> <token type="wtoken"
 * id="s00005_t0005">qui</token> <token type="wtoken"
 * id="s00005_t0006">fÃ»t</token> <token type="wtoken"
 * id="s00005_t0007">bon</token> <token type="wtoken"
 * id="s00005_t0008">j'</token> <token type="wtoken"
 * id="s00005_t0009">en</token> <token type="wtoken"
 * id="s00005_t0010">mange</token> <token type="wtoken"
 * id="s00005_t0011">les</token> <token type="wtoken"
 * id="s00005_t0012">hasards</token> <token type="wtoken"
 * id="s00005_t0013">d'</token> <token type="wtoken"
 * id="s00005_t0014">un</token> <token type="wtoken"
 * id="s00005_t0015">ange</token> <token type="wtoken"
 * id="s00005_t0016">de</token> <token type="filler"
 * id="s00005_t0017">[carillon]</token> </tokens> </sentence> </Token>
 */


#define THRESHOLD1	200
#define THRESHOLD2	400

char            T_punct_s[] =
{
	'.', '!', '?', ':',	/* separateur 'classique' de fin de phrase */
	'\0'
};

char            T_punct_w[] =
{
	';',
	',',
	'<', '>',
	'"',
	'\0'
};

int 
if_ponct(char c, char *tabl)
{
	static int      n;
	for (n = 0; tabl[n]; n++)
		if (c == tabl[n])
			return 1;
	return 0;
}

int 
only_ponct_strong(char *ch)
{
	int             i;
	for (i = 0; (ch[i]) && (if_ponct(ch[i], T_punct_s)); i++);
	return ch[i] ? False : True;
}

int 
only_ponct_weak(char *ch)
{
	int             i;
	for (i = 0; (ch[i]) && (if_ponct(ch[i], T_punct_w)); i++);
	return ch[i] ? False : True;
}

/* ................................................................ */

#define MAX_SIZE_FILE	20000
#define MAX_SIZE_WORD	400
#define MAX_SIZE_SEGM	10000

int 
justspace(char *ch)
{
	int             i;
	for (i = 0; (ch[i]) && ((ch[i] == ' ') || (ch[i] == '\t') || (ch[i] == '\n')); i++);
	return ch[i] ? False : True;
}

int 
load_xxxx(FILE * file, int i, char t_segm[MAX_SIZE_SEGM][MAX_SIZE_WORD], char **chidxx)
{
	static char     ch[TailleLigne], *pt;
	if (!fgets(ch, TailleLigne, file))
		ERREUR("no more XXXX", "");
	pt = strtok(ch, " \t\n");
	(*chidxx) = pt;
	for (pt = strtok(NULL, " \t\n"); pt; pt = strtok(NULL, " \t\n")) {
		if (i == MAX_SIZE_SEGM)
			ERREUR("cste MAX_SIZE_SEGM too small", "");
		if (!strcmp(pt, "&amp;"))
			strcpy(t_segm[i++], "et");
		else if (!strcmp(pt, "&amp"))
			strcpy(t_segm[i++], "et");
		else if (strcmp(pt, "_"))
			strcpy(t_segm[i++], pt);
	}
	return i;
}

/* ................................................................ */

int 
main(int argc, char **argv)
{
	char            ch[TailleLigne], *pt, ch2[TailleLigne], t_file[MAX_SIZE_FILE][MAX_SIZE_WORD],
	                chtext[TailleLigne], t_segm[MAX_SIZE_SEGM][MAX_SIZE_WORD],
	               *chidxx;
	int             nb, idsent, idtoken, itime, i_sent, i, j, i_segm,
	                i_deb, i_fin, minuscule;
	FILE           *file;

	minuscule = False;
	if (argc > 1)
		for (nb = 1; nb < argc; nb++)
			if (!strcmp(argv[nb], "-minuscule")) {
				minuscule = True;
			} else if (!strcmp(argv[nb], "-h")) {
				fprintf(stderr, "Syntax: %s [-h] [-minuscule]\n", argv[0]);
				exit(0);
			} else
				ERREUR("unknown option:", argv[nb]);

	struct tms      time_struct;
	itime = (int) (times(&time_struct));
	sprintf(ch2, "tmp%d.txt", itime);
	if (!(file = fopen(ch2, "wt")))
		ERREUR("can't write in:", ch2);

	for (i_segm = 0, idsent = 1, nb = idtoken = 0; fgets(ch, TailleLigne, stdin); nb++) {
		if (i_sent > MAX_SIZE_FILE - 100)
			ERREUR("cste MAX_SIZE_FILE too small", "");
		strtok(ch, "\n");
		if ((ch[0]) && (ch[0] != '<') && (!justspace(ch))) {
			if (minuscule) {
				sprintf(t_file[i_sent++], "xxxx%d", i_segm);
				fprintf(file, "xxxx%d %s\n", i_segm++, ch);
			} else {
				sprintf(t_file[i_sent++], "XXXX%d", i_segm);
				fprintf(file, "XXXX%d %s\n", i_segm++, ch);
			}
		} else if (ch[0] == '<')
			sprintf(t_file[i_sent++], "%s", ch);
	}
	fclose(file);
	if (minuscule)
		sprintf(ch2, "cat tmp%d.txt | $LIA_NE/src/decapital | \
	$LIA_NE/script/clean_txt.csh -nocap > tmp%d.txt2", itime, itime);
	else
		sprintf(ch2, "cat tmp%d.txt | \
	$LIA_NE/script/clean_txt.csh > tmp%d.txt2", itime, itime);
	system(ch2);
	/*
	 * now we process the table of strings with the file processed by
	 * lia_clean
	 */
	/*
	XXXX0 sept heures
	XXXX1 à l' écoute d'
	XXXX2 RTM Chaîne Inter
	XXXX3 .
	XXXX4 _
	XXXX5 au sommaire de notre première édition d' informations de la journée , la page nationale ,
	XXXX6 sa majesté le roi
	XXXX7 Mohamed
	XXXX8 VI
	*/
	/* for(i=0;i<i_sent;i++) printf("ZOZO: %s\n",t_file[i]) ; */

	sprintf(ch2, "tmp%d.txt2", itime);
	if (!(file = fopen(ch2, "rt")))
		ERREUR("can't read:", ch2);
	for (idsent = 0, idtoken = 1, i = 0; i < i_sent; i++) {
		if (!strncmp(t_file[i], "<Turn", 5)) {
			printf("%s\n", t_file[i]);
			for (i_segm = 0, ++i; (i < i_sent) && (strncmp(t_file[i], "</Turn", 6)); i++) {
				if ((!strncmp(t_file[i], "XXXX", 4)) || (!strncmp(t_file[i], "xxxx", 4))) {
					i_segm = load_xxxx(file, i_segm, t_segm, &chidxx);
					if (strcmp(t_file[i], chidxx))
						ERREUR("mismatch:", t_file[i]);
				} else
					strcpy(t_segm[i_segm++], t_file[i]);
			}
			if (i == i_sent)
				ERREUR("No closing turn", "");

			if (i_segm > 0) {
				for (chtext[0] = '\0', j = i_deb = i_fin = 0; j <= i_segm; j++) {
					/* end of sentence */
					if ((i_deb < i_fin) &&
					    ((j == i_segm) || (only_ponct_strong(t_segm[j])) || ((only_ponct_weak(t_segm[j])) && ((i_fin - i_deb) >= THRESHOLD1)) || ((i_fin - i_deb) >= THRESHOLD2))) {
						if (t_segm[j][0] != '<') {
							strcat(chtext, t_segm[j]);
							strcat(chtext, " ");
						}
						printf("<sentence id=\"s%04d\">\n", ++idsent);
						printf("<text>%s</text>\n", chtext);
						printf("<tokens count=\"%d\">\n", (i_fin - i_deb) + 1);
						for (; i_deb <= i_fin; i_deb++) {
							if (t_segm[i_deb][0] == '<')
								printf("\t<token id=\"s%04d_t%04d\" type=\"sgmltag\">%s</token>\n", idsent, idtoken++, t_segm[i_deb]);
							else if (only_ponct_strong(t_segm[i_deb]))
								printf("\t<token id=\"s%04d_t%04d\" type=\"poncts\">%s</token>\n", idsent, idtoken++, t_segm[i_deb]);
							else if (only_ponct_weak(t_segm[i_deb]))
								printf("\t<token id=\"s%04d_t%04d\" type=\"ponctw\">%s</token>\n", idsent, idtoken++, t_segm[i_deb]);
							else
								printf("\t<token id=\"s%04d_t%04d\" type=\"wtoken\">%s</token>\n", idsent, idtoken++, t_segm[i_deb]);
						}
						printf("</tokens>\n</sentence>\n");
						i_deb++;
					} else {
						if (t_segm[j][0] != '<') {
							strcat(chtext, t_segm[j]);
							strcat(chtext, " ");
						}
					}
					if (i_fin < i_segm - 1)
						i_fin++;
				}
			}
			printf("%s\n", t_file[i]);
		} else
			printf("%s\n", t_file[i]);
	}
	sprintf(ch2, "rm tmp%d.txt tmp%d.txt2", itime, itime);
	system(ch2);
	exit(0);
}
  
