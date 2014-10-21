/*
#    --------------------------------------------------------
#    LIA_TAGG: a statistical POS tagger + syntactic bracketer
#    --------------------------------------------------------
#
#    Copyright (C) 2001 FREDERIC BECHET
#
#    ..................................................................
#
#    This file is part of LIA_TAGG
#
#    LIA_TAGG is free software; you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation; either version 2 of the License, or
#    (at your option) any later version.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program; if not, write to the Free Software
#    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
#    ..................................................................
#
#    Contact :
#              FREDERIC BECHET - LIA - UNIVERSITE D'AVIGNON
#              AGROPARC BP1228 84911  AVIGNON  CEDEX 09  FRANCE
#              frederic.bechet@lia.univ-avignon.fr
#    ..................................................................
*/
/*  Usefule things for the 'gram' module  */
/*  FRED 0498 - Modif multi ML - 0399  */

/*................................................................*/

/*  Les indispensables  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <math.h>
#include <ailleur_siroco.h>
#include <gere_lexique.h>
#include <proba_mot_classe.h>

/*................................................................*/

/*  Type de stockage des 1.2.3-gram  */

#define MARGE	1

#define Nb1Byte	3
typedef struct
	{
	unsigned char cle[Nb1Byte];
	flogprob_t lp,fr;
	} type_1gram;

#define Nb2Byte	6
typedef struct
	{
	unsigned char cle[Nb2Byte];
	flogprob_t lp,fr;
	} type_2gram;

#define Nb3Byte	9
typedef struct
	{
	unsigned char cle[Nb3Byte];
	flogprob_t lp;
	} type_3gram;

/*  Declaration du type ML  */

typedef struct type_ml
	{
	/* un modele proba mot classe (pmc) */
	ty_pmc pmc;
	/* un lexique de la librairie gere_lexique */
	ty_lexique lexique;
	/* Taille des tableaux de Hash : ATTENTION prendre
	   des nombres premiers !! */
	long NB1GRAM,NB2GRAM,NB3GRAM;
	/* les tableaux de Hash */
	type_1gram *TABL1GRAM;
	type_2gram *TABL2GRAM;
	type_3gram *TABL3GRAM;
	/* Si on utilise le hash-code ou la dichotomie */
	int GRAM_SI_HASH;
	/* If we have a 2gram or a 3gram model */
	int GRAM_SI_2G;
	/* If LOG10 or LOGe */
	int GRAM_LOG10;
	} *ty_ml;

/*................................................................*/

/* Variables globales de stockage des ML */

#define NB_MAX_ML       1000

extern ty_ml Table_ML[]; 

/*  Nombre de ML present en memoire  */

extern int NB_ML;

/* Macro permettant de renvoyer un pointeur sur un modele pmc */
#define MODELE_PMC(n)	(Table_ML[(n)]->pmc)

/* Macro permettant de renvoyer un pointeur sur un lexique */
#define LEXIQUE(n)	(Table_ML[(n)]->lexique)

/* Macro permettant de renvoyer un pointeur sur un ML */
#define ML(n)		(Table_ML[(n)])

/* Macro permettant de renvoyer le nombre de 1,2,3 grams */
#define NOMBRE_1GRAM(n)           (Table_ML[(n)]->NB1GRAM)
#define NOMBRE_2GRAM(n)           (Table_ML[(n)]->NB2GRAM)
#define NOMBRE_3GRAM(n)           (Table_ML[(n)]->NB3GRAM)

/* allocation des objets ml */

ty_ml cons_ml(long ,long ,long ,int ,int ,int );

/*................................................................*/

/*  Gestion des NGrams  */

#define BYTE1(a) (a & 0xFF)
#define BYTE2(a) ((a & 0xFF00)>>8)
#define BYTE3(a) ((a & 0xFF0000)>>16)

/*  Test de case vide  */

/* le dernier bit du dernier octet du tableau de cles est utilise
   comme booleen de case vide. Exemple : pour les 1grams, la cle
   est composee de 3 octets, X1111111 11111111 11111111, si le
   bit X est a 1, la case est vide, s'il est a 0 la case est pleine.
   ATTENTION, ce codage limite la taille du vocabulaire a 8388607 mots. */

#define Case1Vide(i,pt_ml) ((pt_ml->TABL1GRAM[i].cle[Nb1Byte-1]&128)>>7)
#define Case2Vide(i,pt_ml) ((pt_ml->TABL2GRAM[i].cle[Nb2Byte-1]&128)>>7)
#define Case3Vide(i,pt_ml) ((pt_ml->TABL3GRAM[i].cle[Nb3Byte-1]&128)>>7)

/*................................................................*/

/*  Addition circulaire  */
/* on doit faire (valh1+(valh2*(essai-1)))%nbgram */

#define EssaiSuivant(valh1,valh2,essai,nbgram) \
((wrd_index_t)fmod(((double)valh1+((double)valh2*(double)(essai-1))),(double)(nbgram)))

/*................................................................*/

/*  Fonction de Hachage  */

#define TETA1	((double)0.6180339887)
#define TETA2	((double)0.3819660113)

/* on ajoute 1 aux valeurs de i pour eviter que la cle soit a 0 */

#define Combine2Indice(i1,i2) (i1+100*(i2+1))
#define Combine3Indice(i1,i2,i3) (i1+100*(i2+1)+100*(i3+1))

/* premiere fonction */
#define H1Value(i,pt_ml) ((long)(fmod((double)((i)+1)*TETA1,(double)1)*(double)(pt_ml)->NB1GRAM)+1)
#define H2Value(i1,i2,pt_ml) ((long)(fmod((double)(Combine2Indice((i1),(i2)))*TETA1,\
(double)1)*(double)(pt_ml)->NB2GRAM)+1)
#define H3Value(i1,i2,i3,pt_ml) ((long)(fmod((double)(Combine3Indice((i1),(i2),(i3)))*TETA1,\
(double)1)*(double)(pt_ml)->NB3GRAM)+1)

/* deuxieme fonction */
#define Double1H(i,pt_ml) ((long)(fmod((double)(i+1)*TETA2,(double)1)*(double)(pt_ml->NB1GRAM-1))+1)
#define Double2H(i1,i2,pt_ml) ((long)(fmod((double)(Combine2Indice(i1,i2))*TETA2,\
(double)1)*(double)(pt_ml->NB2GRAM-1))+1)
#define Double3H(i1,i2,i3,pt_ml) ((long)(fmod((double)(Combine3Indice(i1,i2,i3))*TETA2,\
(double)1)*(double)(pt_ml->NB3GRAM-2))+1)

/*................................................................*/

/*  Egalite  */

int H1compar(const void * ,const void * ) ;
int H2compar(const void * ,const void * ) ;
int H3compar(const void * ,const void * ) ;

int SiEgal1(wrd_index_t  ,long, ty_ml) ;
int SiEgal2(wrd_index_t  ,wrd_index_t ,long, ty_ml) ;
int SiEgal3(wrd_index_t  ,wrd_index_t ,wrd_index_t ,long, ty_ml) ;

/*................................................................*/

/*  Passage des log 10 aux log e  */

#define LOG10_2_LOGe(a)         ((double)(a)*(log((double)10)))
 
/*................................................................*/

/*  Gestion des 1.2.3-gram au format arpa  */

/*  FRED 0398 - Modif multi ML 0399  */

err_t  gram_module_init(char *, int *, int , const err_t);

err_t gram_module_reset(const int, const err_t);

err_t gram_proba_to_bigram(logprob_t *,const wrd_index_t , const wrd_index_t ,
	const int, const err_t );

err_t gram_proba_to_trigram(logprob_t *,const wrd_index_t , const wrd_index_t ,
	const wrd_index_t , const int, const err_t );

int Recherche1Gram(wrd_index_t, flogprob_t *, flogprob_t *, ty_ml);
int HashRecherche1Gram(wrd_index_t, flogprob_t *, flogprob_t *, ty_ml);
int DichoRecherche1Gram(wrd_index_t, flogprob_t *, flogprob_t *, ty_ml);

int Recherche2Gram(wrd_index_t, wrd_index_t, flogprob_t *, flogprob_t *, ty_ml);
int HashRecherche2Gram(wrd_index_t, wrd_index_t, flogprob_t *, flogprob_t *, ty_ml);
int DichoRecherche2Gram(wrd_index_t, wrd_index_t, flogprob_t *, flogprob_t *, ty_ml);

int Recherche3Gram(wrd_index_t, wrd_index_t, wrd_index_t, flogprob_t *, ty_ml);
int HashRecherche3Gram(wrd_index_t, wrd_index_t, wrd_index_t, flogprob_t *, ty_ml);
int DichoRecherche3Gram(wrd_index_t, wrd_index_t, wrd_index_t, flogprob_t *, ty_ml);
  
