/* TRANSF ACCENT -> BDLEX  */ 

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

int PossibleChangement(ch)
 char *ch;
{
/*  Cas des accents  */
if (((ch[0]=='a')||(ch[0]=='A')||
     (ch[0]=='e')||(ch[0]=='E')||
     (ch[0]=='i')||(ch[0]=='I')||
     (ch[0]=='o')||(ch[0]=='O')||
     (ch[0]=='u')||(ch[0]=='U'))&&
    ((ch[1]=='1')||(ch[1]=='2')||
     (ch[1]=='3')||(ch[1]=='4'))) return 1;
/*  Le c cedille  */
if (((ch[0]=='c')||(ch[0]=='C'))&&(ch[1]=='5')) return 1;
/*  Les formes \chiffre  */
if ((ch[0]=='\\')&&(ch[1]>='1')&&(ch[1]<='5')) return 1;
/*  Les formes \\  */
if ((ch[0]=='\\')&&(ch[1]=='\\')) return 1;
return 0;
}

void Accent2Bdlex(chaine)
 char *chaine;
{
char ch[90000];
int n,i;

for(n=i=0;chaine[i];i++)
 {
 switch (chaine[i])
  {
    case 'Å' :
       ch[n] = 'e'; ch[n+1] = '1';
       n+=2;
       break; 
    case 'Ü' :
       ch[n] = 'E'; ch[n+1] = '1';
       n+=2;
       break; 
    case 'É' : 
       ch[n] = 'e'; ch[n+1] = '2';
       n+=2;
       break; 
    case '£' : 
       ch[n] = 'E'; ch[n+1] = '2';
       n+=2;
       break; 
    case 'Á' :
       ch[n] = 'e'; ch[n+1] = '3';
       n+=2;
       break; 
    case '¤' :
       ch[n] = 'E'; ch[n+1] = '3';
       n+=2;
       break; 
    case 'Í' :
       ch[n] = 'e'; ch[n+1] = '4';
       n+=2;
       break; 
    case '¥' :
       ch[n] = 'E'; ch[n+1] = '4';
       n+=2;
       break; 
    case 'È' :
       ch[n] = 'a'; ch[n+1] = '2';
       n+=2;
       break; 
    case '¡' :
       ch[n] = 'A'; ch[n+1] = '2';
       n+=2;
       break; 
    case 'À' :
       ch[n] = 'a'; ch[n+1] = '3';
       n+=2;
       break; 
    case '¢' :
       ch[n] = 'A'; ch[n+1] = '3';
       n+=2;
       break; 
    case 'Ì' :
       ch[n] = 'a'; ch[n+1] = '4';
       n+=2;
       break; 
    case 'Ø' :
       ch[n] = 'A'; ch[n+1] = '4';
       n+=2;
       break; 
    case 'Ñ' :
       ch[n] = 'i'; ch[n+1] = '3';
       n+=2;
       break; 
    case '¦' :
       ch[n] = 'I'; ch[n+1] = '3';
       n+=2;
       break; 
    case 'Ý' :
       ch[n] = 'i'; ch[n+1] = '4';
       n+=2;
       break; 
    case '§' :
       ch[n] = 'I'; ch[n+1] = '4';
       n+=2;
       break; 
    case 'Â' :
       ch[n] = 'o'; ch[n+1] = '3';
       n+=2;
       break; 
    case 'ß' :
       ch[n] = 'O'; ch[n+1] = '3';
       n+=2;
       break; 
    case 'Î' :
       ch[n] = 'o'; ch[n+1] = '4';
       n+=2;
       break; 
    case 'Ú' :
       ch[n] = 'O'; ch[n+1] = '4';
       n+=2;
       break; 
    case 'Ë' :
       ch[n] = 'u'; ch[n+1] = '2';
       n+=2;
       break; 
    case '­' :
       ch[n] = 'U'; ch[n+1] = '2';
       n+=2;
       break; 
    case 'Ã' :
       ch[n] = 'u'; ch[n+1] = '3';
       n+=2;
       break; 
    case '®' :
       ch[n] = 'U'; ch[n+1] = '3';
       n+=2;
       break; 
    case 'Ï' :
       ch[n] = 'u'; ch[n+1] = '4';
       n+=2;
       break; 
    case 'Û' :
       ch[n] = 'U'; ch[n+1] = '4';
       n+=2;
       break; 
    case 'µ' :
       ch[n] = 'c'; ch[n+1] = '5';
       n+=2;
       break;
    case '´' :
       ch[n] = 'C'; ch[n+1] = '5';
       n+=2;
       break;
    default :
       ch[n++] = chaine[i];
       if (PossibleChangement(chaine+i)) ch[n++]='\\';
       break;
  }
 }
ch[n]='\0';
strcpy(chaine,ch);
}

int main()
{
char ch[80000];
while(fgets(ch,80000,stdin))
 {
 Accent2Bdlex(ch);
 printf("%s",ch);
 }

exit(0);
}
 
