/* Convert a CTM file into a TOKEN (MACAON-like) format  */
/* FRED 0309  */

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
 * Format:
 * 
 * CTM format: <SOURCE> <CANAL> <DEBUT> <DUREE> <MOT> [ <CONFIANCE> ]
 * 
 * 20041006_0700_0800_CLASSIQUE 1 1.98 0.19 bonne 20041006_0700_0800_CLASSIQUE 1
 * 2.17 0.54 journÈe 20041006_0700_0800_CLASSIQUE 1 2.71 0.19 bon
 * 20041006_0700_0800_CLASSIQUE 1 2.90 0.82 courage
 * 20041006_0700_0800_CLASSIQUE 1 3.72 0.27 pour 20041006_0700_0800_CLASSIQUE
 * 1 3.99 0.20 ce 20041006_0700_0800_CLASSIQUE 1 4.19 0.46 mercredi--time--1
 * 20041006_0700_0800_CLASSIQUE 1 4.65 0.25 six--time--1
 * 20041006_0700_0800_CLASSIQUE 1 4.90 0.44 octobre--time--1
 * 20041006_0700_0800_CLASSIQUE 1 5.34 0.31 dans 20041006_0700_0800_CLASSIQUE
 * 1 5.65 0.15 une 20041006_0700_0800_CLASSIQUE 1 5.80 0.33 petite
 * 
 * 
 * TOKEN Format: <?xml version="1.0" encoding="UTF-8"?> <Token
 * audio_filename="XX" type="AUTO" asr="LIUM" version="01"
 * version_date="july2008"> <sentence id="s00005"> <text><sil> <sil> et ce(2)
 * qui f√ªt bon j' en(2) mange les hasards d' un(2) ange(2) de(2)
 * [carillon]</text> <tokens count="17"> <token type="sgmltag"
 * id="s00005_t0001"><Sync time="50.00" /></token> <token type="silence"
 * id="s00005_t0002"><sil></token> <token type="wtoken"
 * id="s00005_t0003">et</token> <token type="wtoken"
 * id="s00005_t0004">ce</token> <token type="wtoken"
 * id="s00005_t0005">qui</token> <token type="wtoken"
 * id="s00005_t0006">f√ªt</token> <token type="wtoken"
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
 * 
 */

/* ................................................................ */

#define MAX_SIZE_FILE   20000
#define MAX_SIZE_WORD   100
#define MAX_SIZE_SEGM   30000

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

#define THRESHOLD1      200
#define THRESHOLD2      400

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

int 
only_ponct(char *ch)
{
	return (only_ponct_strong(ch)) || (only_ponct_weak(ch));
}

/* ................................................................ */

/* CTM format: <SOURCE> <CANAL> <DEBUT> <DUREE> <MOT> [ <CONFIANCE> ] */

void 
find_time(char *chtag, double *begin, double *end)
{
	static char     ch[TailleLigne], *pt;
	double          duration;
	int             i;
	for (i = 0; (chtag[i]) && (strncmp(chtag + i, "content=", 8)); i++);
	if (!chtag[i])
		ERREUR("bad format:", chtag);
	strcpy(ch, chtag + i + 9);
	pt = strtok(ch, " \t\n");
	if (!pt)
		ERREUR("bad format:", ch);
	pt = strtok(NULL, " \t\n");
	if (!pt)
		ERREUR("bad format:", ch);
	pt = strtok(NULL, " \t\n");
	if (!pt)
		ERREUR("bad format:", ch);
	if (sscanf(pt, "%lf", begin) != 1)
		ERREUR("bad format:", pt);
	pt = strtok(NULL, " \t\n");
	if (!pt)
		ERREUR("bad format:", ch);
	if (sscanf(pt, "%lf", &duration) != 1)
		ERREUR("bad format:", pt);
	*end = (*begin) + duration;
}

int 
main(int argc, char **argv)
{
	char           *chname, *chasr, *chsource, *chcanal, *chdebut,
	               *chduree, *chmot, chtrans[TailleLigne];
	char            ch[TailleLigne], *pt, ch2[TailleLigne], t_file[MAX_SIZE_FILE][MAX_SIZE_WORD],
	                t_segm[MAX_SIZE_SEGM][MAX_SIZE_WORD], *chidxx;
	int             nb, idsent, idtoken, itime, i_sent, i, j, i_segm, if_minu,
	                i_deb, i_fin, ignore, nbtoken;
	FILE           *filout, *filein, *filetemp;
	double          begin, end, lastend;
	struct tms      time_struct;
	chname = NULL;
	if_minu=False;
	if (argc > 1)
		for (nb = 1; nb < argc; nb++)
			if (!strcmp(argv[nb], "-name")) {
				if (nb + 1 == argc)
					ERREUR("an option must follow option:", argv[nb]);
				chname = argv[++nb];
			} else
			if (!strcmp(argv[nb],"-nocap")) { if_minu=True; }
			else
			if (!strcmp(argv[nb], "-h")) {
				fprintf(stderr, "Syntax: %s [-h] [-name <string> -asr <string> -nocap]\n", argv[0]);
				exit(0);
			} else
				ERREUR("unknown option:", argv[nb]);

	filout = stdout;
	filein = stdin;

	fprintf(filout, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	fprintf(filout, "<Token audio_filename=\"%s\" type=\"%s\" asr=\"%s\" version_date=\"march2009\">\n",
		chname ? chname : "XXXX", chasr ? "AUTO" : "MANUAL", chasr ? chasr : "NONE");
	fprintf(filout, "<Header_CTM>\n");
	while ((fgets(ch, TailleLigne, filein)) && (ch[0] == ';'))
		fprintf(filout, "%s", ch);
	fprintf(filout, "</Header_CTM>\n");


	itime = (int) (times(&time_struct));
	sprintf(ch2, "tmp%d.txt", itime);
	if (!(filetemp = fopen(ch2, "wt")))
		ERREUR("can't write in:", ch2);
	for (i_segm = 0, idsent = 1, nb = 1, idtoken = 0; !feof(filein); nb++) {
		if (i_sent > MAX_SIZE_FILE - 100)
			ERREUR("cste MAX_SIZE_FILE too small", "");
		strtok(ch, "\n");
		chsource = strtok(ch, " \t\n");
		if (!chsource)
			ERREUR("bad format source:", ch);
		chcanal = strtok(NULL, " \t\n");
		if (!chcanal)
			ERREUR("bad format canal:", ch);
		chdebut = strtok(NULL, " \t\n");
		if (!chdebut)
			ERREUR("bad format debut:", ch);
		chduree = strtok(NULL, " \t\n");
		if (!chduree)
			ERREUR("bad format duree:", ch);
		chmot = strtok(NULL, " \t\n");
		if (!chmot)
			ERREUR("bad format mot:", ch);
		sprintf(t_file[i_sent], "<token type=\"sgmltag\" content=\"%s %s %s %s \"/>\n", chsource, chcanal, chdebut, chduree);
		i_sent++;
		sprintf(t_file[i_sent++], "XXXX%d", i_segm);
		fprintf(filetemp, "XXXX%d ", i_segm++);
		fprintf(filetemp, "%s\n", chmot);
		fgets(ch, TailleLigne, filein);
	}
	fclose(filetemp);
	/*
	sprintf(ch2, "cat tmp%d.txt | sed 's/-/ /g' | sed 's/_/ /g' | sed 's/\\&amp;/et/g' | $LIA_BIGLEX_NE/script/lia_clean.biglex_ne | \
	$LIA_NE/src/onelinex | sed 's/ _ / /g' > tmp%d.txt2", itime, itime);
	*/
        sprintf(ch2, "cat tmp%d.txt | $LIA_NE/script/clean_txt.csh %s > tmp%d.txt2", itime, if_minu?"-nocap":"",itime);
	system(ch2);

	/*
	 * now we process the table of strings with the file processed by
	 * lia_clean and we fill t_segm
	 */
	/*
	 * XXXX0 sept heures
	 * XXXX1 ‡ l' Ècoute d'
	*/
	sprintf(ch2, "tmp%d.txt2", itime);
	if (!(filetemp = fopen(ch2, "rt")))
		ERREUR("can't read:", ch2);
	for (i_segm = i = 0; i < i_sent; i++) {
		if (i_segm > MAX_SIZE_FILE - 100)
			ERREUR("cste MAX_SIZE_FILE too small", "");

		if (!strncmp(t_file[i], "XXXX", 4)) {
			i_segm = load_xxxx(filetemp, i_segm, t_segm, &chidxx);
			if (strcmp(t_file[i], chidxx))
				ERREUR("mismatch:", t_file[i]);
		} else
			strcpy(t_segm[i_segm++], t_file[i]);
	}
	fclose(filetemp);
	sprintf(ch2, "rm tmp%d.txt tmp%d.txt2", itime, itime);
	system(ch2);

	/* now we find the 'sentences' */
	for (chtrans[0] = '\0', idsent = idtoken = nbtoken = i_deb = i = 0; i_deb < i_segm;) {
		if (!strncmp(t_segm[i], "<token", 6))
			find_time(t_segm[i], &begin, &end);
		/*
		 * fprintf(stderr,"XOXO %s : lastend=%lf begin=%lf end=%lf
		 * gap=%lf\n",t_segm[i],lastend,begin,end,(begin-lastend));
		 */

		if ((i == i_segm) || (!strcmp(t_segm[i], "</s>")) || (((i - i_deb) > THRESHOLD1) && (only_ponct(t_segm[i]))) || ((i - i_deb) > THRESHOLD2) || ((begin - lastend) > 0.4)) {
			/* debut de phrase */
			if ((i < i_segm) && (t_segm[i][0] != '<'))
				strcat(chtrans, t_segm[i]);
			if ((i < i_segm) && (strcmp(t_segm[i], "</s>")))
				nbtoken++;
			fprintf(filout, "<sentence id=\"s%06d\">\n", ++idsent);
			fprintf(filout, "<text>%s</text>\n", chtrans);
			fprintf(filout, "<tokens count=\"%d\">\n", nbtoken);
			for (j = i_deb; (j < i_segm) && (j <= i); j++)
				if (strcmp(t_segm[j], "</s>")) {
					if (!strncmp(t_segm[j], "<token", 6))
						fprintf(filout, "\t<token id=\"s%06d_t%04d\" %s", idsent, ++idtoken, t_segm[j] + 6);
					else
						fprintf(filout, "\t<token type=\"wtoken\" id=\"s%06d_t%04d\">%s</token>\n", idsent, ++idtoken, t_segm[j]);
				}
			fprintf(filout, "</tokens>\n");
			fprintf(filout, "</sentence>\n");
			i_deb = i + 1;
			nbtoken = 0;
			chtrans[0] = '\0';
		} else {
			if ((i < i_segm) && (t_segm[i][0] != '<')) {
				strcat(chtrans, t_segm[i]);
				strcat(chtrans, " ");
			} nbtoken++;
		}
		if (i < i_segm)
			i++;
		lastend = end;
	}
	fprintf(filout, "</Token>\n");
	exit(0);
}
 
