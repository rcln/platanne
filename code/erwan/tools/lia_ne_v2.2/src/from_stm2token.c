/* Convert a STM file into a TOKEN (MACAON-like) format  */
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
 * STM format: ( <SOURCE> <CANAL <LOCUTEUR> <DEBUT> <FIN> [ <LABEL> ]
 * <TRANSCRIPTION> )
 *
 *
 * ;; Transcriber export by stm.tcl,v 1.21 on ven mar 06 11:37:47 CET 2009 with
 * encoding ISO-8859-1 ;; transcribed by DGA/JJS, version 44 of 090306 ;; ;;
 * CATEGORY "0" "" "" ;; LABEL "O" "Overall" "Overall" ;; ;; CATEGORY "1"
 * "Hub4 Focus Conditions" "" ;; LABEL "F0" "Baseline//Broadcast//Speech" ""
 * ;; LABEL "F1" "Spontaneous//Broadcast//Speech" "" ;; LABEL "F2" "Speech
 * Over//Telephone//Channels" "" ;; LABEL "F3" "Speech in the//Presence
 * of//Background Music" "" ;; LABEL "F4" "Speech Under//Degraded//Acoustic
 * Conditions" "" ;; LABEL "F5" "Speech from//Non-Native//Speakers" "" ;;
 * LABEL "FX" "All other speech" "" ;; CATEGORY "2" "Speaker Sex" "" ;; LABEL
 * "female" "Female" "" ;; LABEL "male"   "Male" "" ;; LABEL "unknown"
 * "Unknown" "" 20070710_1900_1920_inter 1 excluded_region 0.000 100.533
 * <o,,unknown> ignore_time_segment_in_scoring 20070710_1900_1920_inter 1
 * 20070710_1900_1920_inter_speaker_1 100.533 103.006 <o,f3,male> merci �
 * vous d'écouter France Inter , il est 19 heures . 20070710_1900_1920_inter
 * 1 inter_segment_gap 103.006 108.561 <o,f3,> 20070710_1900_1920_inter 1
 * 20070710_1900_1920_inter_speaker_1 108.561 112.025 <o,fx,male> le 18 20
 * continu avec le journal de Mickaël Thébault , bonsoir Mickaël .
 * 20070710_1900_1920_inter 1 Mickaël_Thébault 112.025 112.814 <o,fx,male>
 * bonsoir .
 *
 *
 * TOKEN Format: <?xml version="1.0" encoding="UTF-8"?> <Token
 * audio_filename="XX" type="AUTO" asr="LIUM" version="01"
 * version_date="july2008"> <Header_STM> ;; Transcriber export by stm.tcl,v
 * 1.21 on ven mar 06 11:37:47 CET 2009 with encoding ISO-8859-1 ;;
 * transcribed by DGA/JJS, version 44 of 090306 ;; ;; CATEGORY "0" "" "" ;;
 * LABEL "O" "Overall" "Overall" ;; ;; CATEGORY "1" "Hub4 Focus Conditions"
 * "" ;; LABEL "F0" "Baseline//Broadcast//Speech" "" ;; LABEL "F1"
 * "Spontaneous//Broadcast//Speech" "" ;; LABEL "F2" "Speech
 * Over//Telephone//Channels" "" ;; LABEL "F3" "Speech in the//Presence
 * of//Background Music" "" ;; LABEL "F4" "Speech Under//Degraded//Acoustic
 * Conditions" "" ;; LABEL "F5" "Speech from//Non-Native//Speakers" "" ;;
 * LABEL "FX" "All other speech" "" ;; CATEGORY "2" "Speaker Sex" "" ;; LABEL
 * "female" "Female" "" ;; LABEL "male"   "Male" "" ;; LABEL "unknown"
 * "Unknown" "" </Header_STM> <sentence id="s00005"> <text><sil> <sil> et
 * ce(2) qui fût bon j' en(2) mange les hasards d' un(2) ange(2) de(2)
 * [carillon]</text> <tokens count="17"> <token type="sgmltag"
 * id="s00005_t0001"><Sync time="50.00" /></token> <token type="silence"
 * id="s00005_t0002"><sil></token> <token type="wtoken"
 * id="s00005_t0003">et</token> <token type="wtoken"
 * id="s00005_t0004">ce</token> <token type="wtoken"
 * id="s00005_t0005">qui</token> <token type="wtoken"
 * id="s00005_t0006">fût</token> <token type="wtoken"
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

#define MAX_SIZE_FILE   32000
#define MAX_SIZE_SEGM   32000

int
justspace(char *ch)
{
	int             i;
	for (i = 0; (ch[i]) && ((ch[i] == ' ') || (ch[i] == '\t') || (ch[i] == '\n')); i++);
	return ch[i] ? False : True;
}

int
load_xxxx(FILE * file, int i, char *t_segm[MAX_SIZE_SEGM], char **chidxx)
{
	static char     ch[TailleLigne], *pt;
	if (!fgets(ch, TailleLigne, file))
		ERREUR("no more XXXX", "");
	/* fprintf(stderr, "stm2token **** %s", ch); */
	pt = strtok(ch, " \t\n");
	(*chidxx) = pt;
	for (pt = strtok(NULL, " \t\n"); pt; pt = strtok(NULL, " \t\n")) {
		if (i == MAX_SIZE_SEGM)
			ERREUR("cste MAX_SIZE_SEGM too small", "");
		if (!strcmp(pt, "&amp;"))
			{
			strcpy(t_segm[i++], "et");
			t_segm[i]=(char*)malloc(sizeof(char)*300);
			}
		else if (!strcmp(pt, "&amp"))
                        {
			strcpy(t_segm[i++], "et");
			t_segm[i]=(char*)malloc(sizeof(char)*300);
			}
		else if (strcmp(pt, "_"))
			{
			strcpy(t_segm[i++], pt);
			t_segm[i]=(char*)malloc(sizeof(char)*300);
			}
		/* fprintf(stderr, " %s| ", t_segm[i - 1]); */
	}
	/* fprintf(stderr, "\n ** nb=%d\n", i); */

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

int
main(int argc, char **argv)
{
	char           *chname, *chasr, *chsource, *chcanal, *chlocuteur, *chdebut,
	               *chfin, *chlabel, chtrans[TailleLigne];
	char            ch[TailleLigne], *pt, ch2[TailleLigne], *t_file[MAX_SIZE_FILE], *t_segm[MAX_SIZE_SEGM],
	               *chidxx;
	int             nb, idsent, idtoken, itime, i_sent, i, j, i_segm, i_deb,
	                i_fin, ignore, nbtoken , if_minu;
	FILE           *filout, *filein, *filetemp;
/*fprintf(stderr,"POPO\n");*/
	chname = NULL;
	if_minu=False;
	if (argc > 1)
		for (nb = 1; nb < argc; nb++)
			if (!strcmp(argv[nb], "-name")) {
				if (nb + 1 == argc)
					ERREUR("an option must follow option:", argv[nb]);
				chname = argv[++nb];
			} else
			if (!strcmp(argv[nb], "-nocap")) { if_minu=True; }
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
	fprintf(filout, "<Header_STM>\n");
	while ((fgets(ch, TailleLigne, filein)) && (ch[0] == ';'))
		fprintf(filout, "%s", ch);
	fprintf(filout, "</Header_STM>\n");

	struct tms      time_struct;
	itime = (int) (times(&time_struct));
	sprintf(ch2, "tmp%d.txt", itime);
	if (!(filetemp = fopen(ch2, "wt")))
		ERREUR("can't write in:", ch2);
        t_file[0]=(char*)malloc(sizeof(char)*300);
	for (i_sent=0,i_segm = 0, idsent = 1, nb = 1, idtoken = 0; !feof(filein); nb++) {
		if (i_sent > MAX_SIZE_FILE - 100)
			ERREUR("cste MAX_SIZE_FILE too small", "");
		strtok(ch, "\n");

		ignore = False;
		chsource = strtok(ch, " \t\n");
		if (!chsource)
			ERREUR("bad format source:", ch);
		chcanal = strtok(NULL, " \t\n");
		if (!chcanal)
			ERREUR("bad format canal:", ch);
		chlocuteur = strtok(NULL, " \t\n");
		if (!chlocuteur)
			ERREUR("bad format locuteur:", ch);
		chdebut = strtok(NULL, " \t\n");
		if (!chdebut)
			ERREUR("bad format debut:", ch);
		chfin = strtok(NULL, " \t\n");
		if (!chfin)
			ERREUR("bad format fin:", ch);
		chlabel = strtok(NULL, " \t\n");
		if (!chlabel)
			ERREUR("bad format label:", ch);
		pt = strtok(NULL, "\n");
		if (chlabel[0] == '<') {
			if (pt)
				strcpy(chtrans, pt);
			else
				chtrans[0] = '\0';
		} else {
			strcpy(chtrans, chlabel);
			if (pt) {
				strcat(chtrans, " ");
				strcat(chtrans, pt);
			} chlabel = NULL;
		}
		if (!strcmp(chtrans, "ignore_time_segment_in_scoring")) {
			ignore = True;
			chtrans[0] = '\0';
		}
		sprintf(t_file[i_sent], "<token type=\"sgmltag\" content=\"%s %s %s %s %s", chsource, chcanal, chlocuteur, chdebut, chfin);
		if (chlabel) {
			chlabel[strlen(chlabel) - 1] = '\0';
			sprintf(t_file[i_sent] + strlen(t_file[i_sent]), " [%s]", chlabel + 1);
		}
		if (ignore)
			sprintf(t_file[i_sent] + strlen(t_file[i_sent]), " ignore_time_segment_in_scoring");
		sprintf(t_file[i_sent] + strlen(t_file[i_sent]), "\"/>\n");
		i_sent++;
                t_file[i_sent]=(char*)malloc(sizeof(char)*300);

		if (chtrans[0]) {
			sprintf(t_file[i_sent++], "XXXX%d", i_segm);
                        t_file[i_sent]=(char*)malloc(sizeof(char)*300);
			fprintf(filetemp, "XXXX%d ", i_segm++);
			/* clean trans */
			for (i = 0; chtrans[i]; i++)
				if ((chtrans[i] == '(') || (chtrans[i] == ')') || (chtrans[i] == '*'));
				else if (chtrans[i] == '[') {
					for (i++; (chtrans[i]) && (chtrans[i] != ']'); i++);
					if (!chtrans[i])
						ERREUR("bad format:", chtrans);
				} else
					fprintf(filetemp, "%c", chtrans[i]);
			fprintf(filetemp, "\n");
		}
		fgets(ch, TailleLigne, filein);
	}
	fclose(filetemp);
	/*sprintf(ch2, "cat tmp%d.txt | sed 's/-/_/g' | sed 's/\\&amp;/et/g' | $LIA_BIGLEX_NE/script/lia_clean.biglex_ne | \
	$LIA_NE/src/onelinex | sed 's/ _ / /g' > tmp%d.txt2", itime, itime);*/
	sprintf(ch2, "cat tmp%d.txt | $LIA_NE/script/clean_txt.csh %s> tmp%d.txt2", itime, if_minu?"-nocap":"",itime);
	system(ch2);
	/*
	 * now we process the table of strings with the file processed by
	 * lia_clean and we fill t_segm
	 */
	/*
	 * XXXX0 sept heures
	 * XXXX1 � l' �coute d'
	*/
	sprintf(ch2, "tmp%d.txt2", itime);
	if (!(filetemp = fopen(ch2, "rt")))
		ERREUR("can't read:", ch2);

        t_segm[0]=(char*)malloc(sizeof(char)*300);
	for (i_segm = i = 0; i < i_sent; i++) {
		if (i_segm > MAX_SIZE_FILE - 100)
			ERREUR("cste MAX_SIZE_FILE too small", "");

		if (!strncmp(t_file[i], "XXXX", 4)) {
			i_segm = load_xxxx(filetemp, i_segm, t_segm, &chidxx);
			if (strcmp(t_file[i], chidxx))
				ERREUR("mismatch:", t_file[i]);
		} else {
			if (strlen(t_file[i]) != 0) {
				strcpy(t_segm[i_segm++], t_file[i]);
				t_segm[i_segm]=(char*)malloc(sizeof(char)*300);
			}
		}
	}
	fclose(filetemp);
	sprintf(ch2, "rm tmp%d.txt tmp%d.txt2", itime, itime);
	system(ch2);

	/* now we find the 'sentences' */
	for (chtrans[0] = '\0', idsent = idtoken = nbtoken = i_deb = i = 0; i_deb < i_segm;) {
		if ((i == i_segm) || (!strcmp(t_segm[i], "</s>")) || (((i - i_deb) > THRESHOLD1) && (only_ponct(t_segm[i]))) || ((i - i_deb) > THRESHOLD2)) {
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
	}
	fprintf(filout, "</Token>\n");
	fclose(filout);

	exit(0);
}
  
