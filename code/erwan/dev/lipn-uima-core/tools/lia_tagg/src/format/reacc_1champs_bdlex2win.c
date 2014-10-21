/*  Affiche avec les accents  */

#include <stdio.h>
#include <string.h>

void accent_mot(ch)
 char *ch;
{
char temp[90000];
int i,n;

for(n=0,i=0;ch[n];n++)
 {
 switch (ch[n+1])
  {
  case '1' :
	switch (ch[n])
	 {
	 case 'e' : temp[i++]='é'; n++; break;
	 case 'E' : temp[i++]='É'; n++; break;
	 default  : { if (ch[n]=='\\') n++; temp[i++]=ch[n]; }
	 }
	break;
  case '2' :
	switch (ch[n])
	 {
	 case 'a' : temp[i++]='à'; n++; break;
	 case 'A' : temp[i++]='À'; n++; break;
	 case 'e' : temp[i++]='è'; n++; break;
	 case 'E' : temp[i++]='È'; n++; break;
	 case 'u' : temp[i++]='ù'; n++; break;
	 case 'U' : temp[i++]='Ù'; n++; break;
	 default  : { if (ch[n]=='\\') n++; temp[i++]=ch[n]; }
	 }
	break;
  case '3' :
	switch (ch[n])
	 {
	 case 'a' : temp[i++]='â'; n++; break;
	 case 'A' : temp[i++]='Â'; n++; break;
	 case 'e' : temp[i++]='ê'; n++; break;
	 case 'E' : temp[i++]='Ê'; n++; break;
	 case 'i' : temp[i++]='î'; n++; break;
	 case 'I' : temp[i++]='Î'; n++; break;
	 case 'o' : temp[i++]='ô'; n++; break;
	 case 'O' : temp[i++]='Ô'; n++; break;
	 case 'u' : temp[i++]='û'; n++; break;
	 case 'U' : temp[i++]='Û'; n++; break;
	 default  : { if (ch[n]=='\\') n++; temp[i++]=ch[n]; }
	 }
	break;
  case '4' :
	switch (ch[n])
	 {
	 case 'a' : temp[i++]='ä'; n++; break;
	 case 'A' : temp[i++]='Ä'; n++; break;
	 case 'e' : temp[i++]='ë'; n++; break;
	 case 'E' : temp[i++]='Ë'; n++; break;
	 case 'i' : temp[i++]='ï'; n++; break;
	 case 'I' : temp[i++]='Ï'; n++; break;
	 case 'o' : temp[i++]='ö'; n++; break;
	 case 'O' : temp[i++]='Ö'; n++; break;
	 case 'u' : temp[i++]='ü'; n++; break;
	 case 'U' : temp[i++]='Ü'; n++; break;
	 default  : { if (ch[n]=='\\') n++; temp[i++]=ch[n]; }
	 }
	break;
  case '5' :
	switch (ch[n])
	 {
	 case 'c' : temp[i++]='ç'; n++; break;
	 case 'C' : temp[i++]='Ç'; n++; break;
	 default  : { if (ch[n]=='\\') n++; temp[i++]=ch[n]; }
	 }
	break;
  case 'Æ' :
        temp[i++]='Ó'; 
	n++;
	break;
  case '°' :
        temp[i++]='³'; 
	n++;
	break;
  default  :
	{
 	if ((ch[n]=='\\')&&((ch[n+1]=='\\')||((ch[n+1]>='1')&&(ch[n+1]<='5')))) n++;
 	temp[i++]=ch[n];
	}
  }
 }
temp[i]='\0';
strcpy(ch,temp);
}

int main(argc,argv)
 int argc;
 char **argv;
{
char mot[80000],ch[80000];
int i;

while (fgets(ch,80000,stdin))
 {
 for(i=0;(ch[i])&&(ch[i]!=' ')&&(ch[i]!='\t');i++) mot[i]=ch[i];
 mot[i]='\0';
 accent_mot(mot);
 printf("%s%s",mot,ch+i);
 }
return 0;
}
  
