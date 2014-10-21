/* sort a list of tokens, and count each occurrence */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <lia_liblex.h>

/*................................................................*/

int main(int argc, char **argv)
{
int nb;
nb=load_lexicon_flux2sort(stdin);
print_lexicon(nb);
exit(0);
}
  
