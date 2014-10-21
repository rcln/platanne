## MAKEFILE LIA_NE
##

CC=gcc
# CFLAGS= -ansi  -I. -I/usr/include/libxml2 -I. -I.
CFLAGS= -I/usr/include/libxml2 -I.

LDFLAGS= -L. -L/opt/local/lib -L/usr/local/lib -L/usr/lib -l xml2 

EXEC= clean_ne_form.exe decapital_biglex.exe decapital.exe tagg_xml_tk.exe filter_ecg.exe fmt4crf.exe from_ctm2token.exe from_ecg2corpuscate.exe from_stm2token.exe from_token2crf.exe from_token2ctm_ne.exe from_token2ecg.exe from_token2stm_ne.exe from_trs2ne_form.exe from_trs2token.exe onelinex.exe postprocess_ne.exe postprocess_ne_ester.exe rewrite_pattern.exe rewrite_token_ne.exe tagg2text.exe merge_biglex.exe edy_recap.exe

SRC= clean_ne_form.c decapital_biglex.c decapital.c tagg_xml_tk.c filter_ecg.c fmt4crf.c from_ctm2token.c from_ecg2corpuscate.c from_stm2token.c from_token2crf.c from_token2ctm_ne.c from_token2ecg.c from_token2stm_ne.c from_trs2ne_form.c from_trs2token.c lia_liblex.c onelinex.c postprocess_ne.c postprocess_ne_ester.c rewrite_pattern.c rewrite_token_ne.c tagg2text.c merge_biglex.c edy_recap.c

OBJ= $(SRC:.c=.o)

all: $(OBJ) $(EXEC)

%o: %c
	$(CC) -o $@ -c $< $(CFLAGS)

from_token2ctm_ne.exe: from_token2ctm_ne.o
	$(CC) -o ../bin/from_token2ctm_ne from_token2ctm_ne.o $(LDFLAGS)

from_token2stm_ne.exe: from_token2stm_ne.o
	$(CC) -o ../bin/from_token2stm_ne from_token2stm_ne.o $(LDFLAGS)

tagg_xml_tk.exe: tagg_xml_tk.o
	$(CC) -o ../bin/tagg_xml_tk tagg_xml_tk.o $(LDFLAGS)

merge_biglex.exe: lia_liblex.o merge_biglex.o
	$(CC) -o ../bin/merge_biglex merge_biglex.o lia_liblex.o

postprocess_ne_ester.exe: lia_liblex.o postprocess_ne_ester.o
	$(CC) -o ../bin/postprocess_ne_ester lia_liblex.o postprocess_ne_ester.o

edy_recap.exe: lia_liblex.o edy_recap.o
	$(CC) -o ../bin/edy_recap lia_liblex.o edy_recap.o

postprocess_ne.exe: lia_liblex.o postprocess_ne.o
	$(CC) -o ../bin/postprocess_ne lia_liblex.o postprocess_ne.o

%exe: %o
	$(CC) -o ../bin/$(@:.exe= ) $<

clean:
	rm -rf *.o
	rm ../bin/*
  
