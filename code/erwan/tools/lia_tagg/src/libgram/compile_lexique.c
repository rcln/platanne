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
/*  Gestion d'un lexique :
      * Pour la compilation :
       - en entree : un fichier lexique compose de <code> <mot>
       - en sortie : 3 fichiers dico.des dico.tab et dico.zon
         avec : dico.des=descripteur de taille du dico
                dico.tab=table des indices des mots du dico trie par alpha
                dico.tab_graf=table des indices des mots du dico trie par code
                dico.zon=stockage des graphies
      * Pour l'utilisation :
       - en entree : 3 fichiers dico.des dico.tab et dico.zon
       - une fonction Mot2Code faisant la correspondance entre mot et code  */
/*  FRED 0498  :  Modif Multi ML - 0399  */

#include <libgram.h>

int main(int argc,char **argv)
{
ty_lexique pt_lexique;

if (argc<2)
 {
 fprintf(stderr,"Syntaxe : %s [-h] <fich lexique>\n",argv[0]);
 exit(0);
 }

if (!strcmp(argv[1],"-h"))
 {
 fprintf(stderr,"Syntaxe : %s [-h] <fich lexique>\n\
 \t ce programme permet de compiler un lexique (format sirocco)\n\
 \t Les arguments d'entree sont les suivants :\n\
 \t  -h : affiche ce message\n\
 \t  fich lexique : un fichier lexique compose de <code> <mot> (sur chaque ligne )\n\
 \t Ce programme produit en sortie 4 fichiers :\n\
 \t  <fich lexique>.des : descripteur\n\
 \t  <fich lexique>.tab : tableau des mots classe alpha\n\
 \t  <fich lexique>.tab_graf : tableau des mots classe code\n\
 \t  <fich lexique>.zon : les graphies\n\n",argv[0]);
 exit(0);
 }

fprintf(stderr,"Compilation du lexique\n");
pt_lexique=CompileLexique(argv[1]);
fprintf(stderr,"Termine\n");

delete_lexique(pt_lexique);

}
 
