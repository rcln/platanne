/*--------------------------------- -*/ 
#include <stdio.h> 
#include <stdlib.h> 
#include <string.h> 
#define UT_DOS      159              /* оо */
#define UT_UNIX   207
#define EA_DOS      142               /* ех */
#define EA_UNIX   197
#define AC_DOS      137              /* ю */
#define AC_UNIX  192
#define AG_DOS     136             /* х */
#define AG_UNIX  200
#define CC_DOS     141              /* ╣ */
#define CC_UNIX  181
#define EC_DOS     144              /* а */
#define EC_UNIX  193
#define ET_DOS      145             /* м */
#define ET_UNIX  205
#define EG_DOS     143            /* и */
#define EG_UNIX  201
#define IT_DOS      149            /* щ */
#define IT_UNIX   221
#define IC_DOS      148            /* я */
#define IC_UNIX   209
#define OC_DOS      153            /* б */
#define OC_UNIX  194
#define UC_DOS      158           /* ц */
#define UC_UNIX  195
#define UG_DOS      157           /* к */
#define UG_UNIX  203
#define LI_DOS       163          /* ╩ */
#define LI_UNIX    187
#define NU_DOS      161           /* Ё */
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
           case UT_DOS : carout = UT_UNIX; break;     /* о */
           case EA_DOS : carout = EA_UNIX; break;     /* е */
           case AC_DOS : carout = AC_UNIX; break;     /* ю */
           case AG_DOS : carout = AG_UNIX; break;     /* х*/
           case CC_DOS : carout = CC_UNIX; break;     /* ╣*/
           case EC_DOS : carout = EC_UNIX; break;     /* а*/
           case ET_DOS : carout = ET_UNIX; break;     /* м*/
           case EG_DOS : carout = EG_UNIX; break;     /* и*/
           case IT_DOS : carout = IT_UNIX; break;     /* щ*/
           case IC_DOS : carout = IC_UNIX; break;     /* я*/
           case OC_DOS : carout = OC_UNIX; break;     /* б*/
           case UC_DOS : carout = UC_UNIX; break;     /* ц*/
           case UG_DOS : carout = UG_UNIX; break;     /* к */
           case LI_DOS : carout = LI_UNIX; break;     /* ╩ */
           case NU_DOS : carout = NU_UNIX; break;     /* Ё */
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
