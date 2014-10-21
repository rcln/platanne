# LIA_NE v2.2
# FRED 07/2010

Alors, tout d'abord il faut les outils suivant (en plus de LIA_NE v2.2) :
- lia_tagg (dispo sur ma page)
- CRF++ (dispo a : http://crfpp.sourceforge.net/)
(et le convertisseur de caractere 'iconv')

Ensuite, il faut definir les variables d'environnement suivante (bien sur la ou les packages sont installes !!) :

Par exemple en bash:

export LIA_TAGG_LANG=french
export LIA_TAGG=/home/tools/lia_tagg
export LIA_NE=/home/tools/lia_ne_v2.2

Apres avoir installe les outils lia_tagg et CRF++ (voir les fichiers README de chacun d'eux),
il suffit d'aller dans le repertoire LIA_NE puis de lancer 'make all'
Ca compile les sources et produits les fichiers de donnees necessaires.

Et enfin, les scripts suivants permettent de traiter des fichiers de reco (CTM et STM) :

script/lia_ne_tagg_ctm <file in ctm> <file out ctm-ne> [-post] [-nocap]
script/lia_ne_tagg_stm <file in stm> <file out stm-ne> [-post] [-nocap]

L'option -post permet d'appliquer les regles de postprocessing specifiques a ESTER.
L'option -nocap permet de prendre en entree un texte sans majuscule.

Par exemple :

./script/lia_ne_tagg_ctm 20071220_1900_1920_inter.ctm popo.ctm-ne -post -nocap

On peut aussi tagger du texte simple, par exemple :

echo "Je vais à Marseille voir l'Olympique de Marseille." | $LIA_NE/script/lia_ne_tagg_txt

qui affichera :

<s> Je vais à <LOC> Marseille </LOC> voir l' <ORG> Olympique de Marseille </ORG> . </s>

ou encore : cat sample.txt | ./script/lia_ne_tagg_txt

Attention, le post-processing ESTER n'est pas applique ici.

----------------------------------------------------------------
ATTENTION !!!!
L'ENCODAGE DES CARACTERES DANS TOUT TEXTE (OU CTM OU STM) DOIT IMPERATIVEMENT ETRE EN ISO-8859-1
UTILISER LA COMMANDE 'iconv -t ISO-8859-1 -f UTF-8' SI VOUS VOULEZ TRAITER DU TEXTE EN UTF-8
----------------------------------------------------------------
 
 
Frederic Bechet
Frederic.Bechet@lif.univ-mrs.fr
  
