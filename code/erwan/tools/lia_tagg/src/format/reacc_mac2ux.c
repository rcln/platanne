/*--------------------------------- -*/ 
#include <stdio.h> 
#include <stdlib.h> 
#include <string.h> 
#define UT_DOS      159              /* �� */
#define UT_UNIX   207
#define EA_DOS      142               /* �� */
#define EA_UNIX   197
#define AC_DOS      137              /* � */
#define AC_UNIX  192
#define AG_DOS     136             /* � */
#define AG_UNIX  200
#define CC_DOS     141              /* � */
#define CC_UNIX  181
#define EC_DOS     144              /* � */
#define EC_UNIX  193
#define ET_DOS      145             /* � */
#define ET_UNIX  205
#define EG_DOS     143            /* � */
#define EG_UNIX  201
#define IT_DOS      149            /* � */
#define IT_UNIX   221
#define IC_DOS      148            /* � */
#define IC_UNIX   209
#define OC_DOS      153            /* � */
#define OC_UNIX  194
#define UC_DOS      158           /* � */
#define UC_UNIX  195
#define UG_DOS      157           /* � */
#define UG_UNIX  203
#define LI_DOS       163          /* � */
#define LI_UNIX    187
#define NU_DOS      161           /* � */
#define NU_UNIX   179
/* ------------------- --------------- */ 
    void help (void) {
printf ("-------------------------------------------\n");
printf (" Conversion d'accents MAC -> UNIX -\n");
printf ("    Usage:   reacc_mac2ux fich_mac fich_unix \n");
printf ("-------------------------------------------\n");
}
/* ------------------- --------------- */ 
int main (int argc, char * argv []) {
char  fdicoin [256], fdicout [256];
unsigned char carin, carout;
long int icar;
FILE * dicoin, * dicout;

if (argc != 3) {
   help();
   return 1;
}

strcpy (fdicoin, argv [1]);
printf ("Ouverture fichier entree %s\n", fdicoin);
if ((dicoin = fopen (fdicoin, "r")) == NULL) {
       printf ("Pb ouverture fichier %s\n", fdicoin);
}
 
strcpy (fdicout, argv [2]);
icar = 0;
while ( ((carin = (unsigned char) fgetc (dicoin))) & (! feof (dicoin))) {
      if (icar == 0) {
	 printf ("Ouverture fichier sortie %s\n", fdicout);
         if ((dicout = fopen (fdicout, "w")) == NULL) {
                printf ("Pb ouverture fichier %s\n", fdicout);
         }
      }
     carout = carin;
     switch (carin) {
           case UT_DOS : carout = UT_UNIX; break;     /* � */
           case EA_DOS : carout = EA_UNIX; break;     /* � */
           case AC_DOS : carout = AC_UNIX; break;     /* � */
           case AG_DOS : carout = AG_UNIX; break;     /* �*/
           case CC_DOS : carout = CC_UNIX; break;     /* �*/
           case EC_DOS : carout = EC_UNIX; break;     /* �*/
           case ET_DOS : carout = ET_UNIX; break;     /* �*/
           case EG_DOS : carout = EG_UNIX; break;     /* �*/
           case IT_DOS : carout = IT_UNIX; break;     /* �*/
           case IC_DOS : carout = IC_UNIX; break;     /* �*/
           case OC_DOS : carout = OC_UNIX; break;     /* �*/
           case UC_DOS : carout = UC_UNIX; break;     /* �*/
           case UG_DOS : carout = UG_UNIX; break;     /* � */
           case LI_DOS : carout = LI_UNIX; break;     /* � */
           case NU_DOS : carout = NU_UNIX; break;     /* � */
           case 131    : carout = 'E';     break;     /* E */
           case 233    : carout = 'E';     break;     /* E */
     } /* switch  */
     fputc (carout, dicout); /*printf("%c %d\n",carin,carin);*/
     icar ++;
 } /* while */
fclose (dicoin);
fclose (dicout);
printf ("- fichier %s converti en %s (%ld caracteres)\n", fdicoin, fdicout, icar);
return 0;
} /* end main */
