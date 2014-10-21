/* TRANSF ACCENT -> BDLEX  */ 

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
    case 'é' :
       ch[n] = 'e'; ch[n+1] = '1';
       n+=2;
       break; 
    case 'É' :
       ch[n] = 'E'; ch[n+1] = '1';
       n+=2;
       break; 
    case 'è' : 
       ch[n] = 'e'; ch[n+1] = '2';
       n+=2;
       break; 
    case 'È' : 
       ch[n] = 'E'; ch[n+1] = '2';
       n+=2;
       break; 
    case 'ê' :
       ch[n] = 'e'; ch[n+1] = '3';
       n+=2;
       break; 
    case 'Ê' :
       ch[n] = 'E'; ch[n+1] = '3';
       n+=2;
       break; 
    case 'ë' :
       ch[n] = 'e'; ch[n+1] = '4';
       n+=2;
       break; 
    case 'Ë' :
       ch[n] = 'E'; ch[n+1] = '4';
       n+=2;
       break; 
    case 'à' :
       ch[n] = 'a'; ch[n+1] = '2';
       n+=2;
       break; 
    case 'À' :
       ch[n] = 'A'; ch[n+1] = '2';
       n+=2;
       break; 
    case 'â' :
       ch[n] = 'a'; ch[n+1] = '3';
       n+=2;
       break; 
    case 'Â' :
       ch[n] = 'A'; ch[n+1] = '3';
       n+=2;
       break; 
    case 'ä' :
       ch[n] = 'a'; ch[n+1] = '4';
       n+=2;
       break; 
    case 'Ä' :
       ch[n] = 'A'; ch[n+1] = '4';
       n+=2;
       break; 
    case 'î' :
       ch[n] = 'i'; ch[n+1] = '3';
       n+=2;
       break; 
    case 'Î' :
       ch[n] = 'I'; ch[n+1] = '3';
       n+=2;
       break; 
    case 'ï' :
       ch[n] = 'i'; ch[n+1] = '4';
       n+=2;
       break; 
    case 'Ï' :
       ch[n] = 'I'; ch[n+1] = '4';
       n+=2;
       break; 
    case 'ô' :
       ch[n] = 'o'; ch[n+1] = '3';
       n+=2;
       break; 
    case 'Ô' :
       ch[n] = 'O'; ch[n+1] = '3';
       n+=2;
       break; 
    case 'ö' :
       ch[n] = 'o'; ch[n+1] = '4';
       n+=2;
       break; 
    case 'Ö' :
       ch[n] = 'O'; ch[n+1] = '4';
       n+=2;
       break; 
    case 'ù' :
       ch[n] = 'u'; ch[n+1] = '2';
       n+=2;
       break; 
    case 'Ù' :
       ch[n] = 'U'; ch[n+1] = '2';
       n+=2;
       break; 
    case 'û' :
       ch[n] = 'u'; ch[n+1] = '3';
       n+=2;
       break; 
    case 'Û' :
       ch[n] = 'U'; ch[n+1] = '3';
       n+=2;
       break; 
    case 'ü' :
       ch[n] = 'u'; ch[n+1] = '4';
       n+=2;
       break; 
    case 'Ü' :
       ch[n] = 'U'; ch[n+1] = '4';
       n+=2;
       break; 
    case 'ç' :
       ch[n] = 'c'; ch[n+1] = '5';
       n+=2;
       break;
    case 'Ç' :
       ch[n] = 'C'; ch[n+1] = '5';
       n+=2;
       break;
    case 'Æ' :
       ch[n] = 'Ó';
       n++;
       break;
    case '°' :
       ch[n] = '³';
       n++;
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
char ch[80000],sauv_cate[1000],*pt;
while(fgets(ch,80000,stdin))
 {
 pt=strtok(ch," \n"); if (pt) pt=strtok(NULL,"\n");
 if (pt) strcpy(sauv_cate,pt); else sauv_cate[0]='\0';
 Accent2Bdlex(ch);
 if (*sauv_cate) printf("%s %s\n",ch,sauv_cate); else printf("%s\n",ch);
 }
return 0;
}
 
