/*  Bibliotheque de gestion d'arbres AVL  */
/*  FRED 1199  */
/*  MODIF 0600 : traitement des chaines de caracteres et compte des infos
 *  Modif 0703 : lia_recherche_avl return the node or NULL instead of 'int'
 *               a new function copying all the nodes of a tree in an array
 *               and sorting them according to their frequency
 *  Modif 0704 : the field 'nb' can also be a code associated to a word  */


/*................................................................*/

#define False	0
#define True	1

#define LIA_MAX_TAILLE_MESSAGE	4000

extern int LIA_AVL_NB_NODE;

/*................................................................*/

/* declaration du type noeud des arbres AVL */

typedef struct lia_avl_type
	{
	signed char dq;
	char *info;
	int nb;
	struct lia_avl_type *fg,*fd;
	} *lia_avl_t;

/*................................................................*/

/* les rotations */

/* parametres :
	1- lia_avl_t = racine de l'arbre */
/* retour : True=rotation effectuee / False=rotation impossible */
int lia_rotation_avl_droite(lia_avl_t);
int lia_rotation_avl_gauche(lia_avl_t);
int lia_rotation_avl_gauche_droite(lia_avl_t);
int lia_rotation_avl_droite_gauche(lia_avl_t);

/*................................................................*/

/* l'insertion d'un element */

/* parametres :
	1- lia_avl_t = racine de l'arbre
	2- char * = info a ajouter a l'arbre
	3- int = booleen (True ou False) avec True=ajout avec reequilibrage
	4- char * = chaine de caractere recevant la trace de l'ajout */
/* retour : lia_avl_t = racine de l'arbre modifie */
lia_avl_t lia_ajoute_element_avl(lia_avl_t, char *, int, char *);

/* insertion BIS : le champs 'nb' est utilise comme un code */
lia_avl_t lia_ajoute_element_code_avl(lia_avl_t, char *,int , int, char *);

/*................................................................*/

/* la suppression d'un element */

/* parametres :
	1- lia_avl_t = racine de l'arbre
	2- char * = info a supprimer de l'arbre
	3- int = booleen (True ou False) avec True=suppression avec reequilibrage
	4- char * = chaine de caractere recevant la trace de la suppression */
/* retour : lia_avl_t = racine de l'arbre modifie */
lia_avl_t lia_supprime_element_avl(lia_avl_t, char *, int, char *);

/*................................................................*/

/* la liberation de la place memoire de l'arbre */

/* parametres :
	1- lia_avl_t = racine de l'arbre */
void lia_libere_avl(lia_avl_t);

/*................................................................*/

/* la recherche d'un element */

/* parametres :
	1- lia_avl_t = racine de l'arbre
	2- char * = info a rechercher dans l'arbre
	3- int * = nb d'occurence de la chaine */
/* retour : the node containing the info or NULL */
lia_avl_t lia_recherche_avl(lia_avl_t, char *,int *);

/*................................................................*/

/* l'affichage de l'arbre */

/* parametres :
	1- lia_avl_t = racine de l'arbre a afficher */
void lia_affiche_avl(lia_avl_t);

void lia_affiche_avl_simple(lia_avl_t ,FILE *);
 
/*................................................................*/

/* copy all the nodes of a tree into an array and sort them
 * according to their frequency */

/* parameters:
 *  1- lia_avl_t = root of the tree to copy
 *  2- int * = return value containing the size of the array */
/* return : the adress of the table containing all the nodes sorted */

lia_avl_t *lia_avl_tree2table_freq(lia_avl_t , int *);

/*................................................................*/

/* binary search, according to the code (or freq) on the table of nodes:
 *  1- lia_avl_t = adress of the node table (obtained with lia_avl_tree2table_freq)
 *  2- int = size of the table (# of elements)
 *  3- int = code or freq looked for  */
lia_avl_t *lia_avl_code2word(lia_avl_t *, int, int);

