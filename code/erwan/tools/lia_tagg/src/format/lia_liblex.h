/*  Managing a lexicon with IDs  */

/* load a lexicon and return a lexicon ID
 *  - input = filename (char *)
 *  - output = lexicon ID (int) */
int load_lexicon(char *);
int load_lexicon_flux2sort(FILE *);

/* delete a lexicon
 *  - input = lexicon ID (int)
 *  - output = void */
void delete_lexicon(int);

/* get a string from a code
 *  - input = lexicon ID (int) + code (int)
 *  - output = 0 if the code is missing
 *             1 if the code is here
 *             the adress of the word string in (char **) */
int code2word(int,int,char**);

/* get a code from a string
 *  - input = lexicon ID (int) + word string (char*)
 *  - output = 0 if the word is not in the lexicon
 *             1 if the word is in the lexicon
 *             the code found in (int*) */
int word2code(int,char*,int*);

#define False	0
#define True	1
void ERREUR(char *, char *);
void print_lexicon(int); 
 
