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
	 case 'e' : temp[i++]='�'; n++; break;
	 case 'E' : temp[i++]='�'; n++; break;
	 default  : { if (ch[n]=='\\') n++; temp[i++]=ch[n]; }
	 }
	break;
  case '2' :
	switch (ch[n])
	 {
	 case 'a' : temp[i++]='�'; n++; break;
	 case 'A' : temp[i++]='�'; n++; break;
	 case 'e' : temp[i++]='�'; n++; break;
	 case 'E' : temp[i++]='�'; n++; break;
	 case 'u' : temp[i++]='�'; n++; break;
	 case 'U' : temp[i++]='�'; n++; break;
	 default  : { if (ch[n]=='\\') n++; temp[i++]=ch[n]; }
	 }
	break;
  case '3' :
	switch (ch[n])
	 {
	 case 'a' : temp[i++]='�'; n++; break;
	 case 'A' : temp[i++]='�'; n++; break;
	 case 'e' : temp[i++]='�'; n++; break;
	 case 'E' : temp[i++]='�'; n++; break;
	 case 'i' : temp[i++]='�'; n++; break;
	 case 'I' : temp[i++]='�'; n++; break;
	 case 'o' : temp[i++]='�'; n++; break;
	 case 'O' : temp[i++]='�'; n++; break;
	 case 'u' : temp[i++]='�'; n++; break;
	 case 'U' : temp[i++]='�'; n++; break;
	 default  : { if (ch[n]=='\\') n++; temp[i++]=ch[n]; }
	 }
	break;
  case '4' :
	switch (ch[n])
	 {
	 case 'a' : temp[i++]='�'; n++; break;
	 case 'A' : temp[i++]='�'; n++; break;
	 case 'e' : temp[i++]='�'; n++; break;
	 case 'E' : temp[i++]='�'; n++; break;
	 case 'i' : temp[i++]='�'; n++; break;
	 case 'I' : temp[i++]='�'; n++; break;
	 case 'o' : temp[i++]='�'; n++; break;
	 case 'O' : temp[i++]='�'; n++; break;
	 case 'u' : temp[i++]='�'; n++; break;
	 case 'U' : temp[i++]='�'; n++; break;
	 default  : { if (ch[n]=='\\') n++; temp[i++]=ch[n]; }
	 }
	break;
  case '5' :
	switch (ch[n])
	 {
	 case 'c' : temp[i++]='�'; n++; break;
	 case 'C' : temp[i++]='�'; n++; break;
	 default  : { if (ch[n]=='\\') n++; temp[i++]=ch[n]; }
	 }
	break;
  case '�' :
        temp[i++]='�'; 
	n++;
	break;
  case '�' :
        temp[i++]='�'; 
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
  
