/*  Tagg a token file with LIA_TAGG+LIA_NE and produce a NE file  */
/*  FRED 0109 + 0910  */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <libxml/parser.h>
#include <libxml/tree.h>
#include <unistd.h>
#include <sys/times.h>

#ifdef LIBXML_TREE_ENABLED

/*................................................................*/

#define TailleLigne     80000

#define True    1
#define False   0

void ERREUR(char *ch1,char *ch2)
{
fprintf(stderr,"ERREUR : %s %s\n",ch1,ch2);
exit(0);
}

int if_letter(char c)
{
if ((c>='a')&&(c<='z')) return True;
if ((c>='A')&&(c<='Z')) return True;
return False;
}

/*................................................................*/

void sprint_word(char *ch, xmlNode *node)
{
if (node)
 {
 if (node->content)
  {
  int i,j;
  char *chin;
  if ((ch[0])&&(ch[strlen(ch)-1]!=' ')) strcat(ch," ");
  for(i=0,j=strlen(ch),chin=(char*)node->content;chin[i];i++)
   {
   if (chin[i]!='\n')
    if ((i>0)&&(chin[i]==' ')&&(chin[i-1]==' ')) ;
    else ch[j++]=chin[i];
   }
  ch[j]='\0';
  }
 sprint_word(ch,node->next);
 sprint_word(ch,node->children);
 }
}

char * find_attribute(xmlAttr *ptat, char *name)
{
for(;(ptat)&&(strcmp((char*)(ptat->name),name));ptat=ptat->next);
if ((!ptat)||(ptat->children==NULL)||(ptat->children->content==NULL)) ERREUR("corpus without ",name);
return (char*)ptat->children->content;
}

xmlNode *find_node(xmlNode * a_node, char *name)
{
xmlNode *cur_node = NULL,*resu;
for (cur_node = a_node; cur_node; cur_node = cur_node->next)
 if ((cur_node->type == XML_ELEMENT_NODE)&&(!strcmp((char*)cur_node->name,name))) return cur_node;
 else 
  {
  resu=find_node(cur_node->children,name);
  if (resu) return resu;
  }
return NULL;
}

void write_text_to_tag(xmlNode * a_node, FILE *fileword, FILE *fileid)
{
xmlNode *cur_node = NULL,*pt,*pt2;
xmlAttr *ptat;
int i,nbtoken,nbword;
static char ch[10*TailleLigne];
static char ch2[TailleLigne];
static int nonempty=True;

for (cur_node = a_node; cur_node; cur_node = cur_node->next)
 {
 if (cur_node->type == XML_ELEMENT_NODE)
  {
  if (!strcmp((char*)cur_node->name,"sentence"))
   {
   if (nonempty) { fprintf(fileword,"<s>\n"); fprintf(fileid,"<s>\n"); nonempty=False; }
   ch[0]='\0';

   pt=find_node(cur_node->children,"tokens");
   if (!pt) ERREUR("bad format in xml: no 'tokens'","");
   for(nbtoken=nbword=0,pt=pt->children;pt;pt=pt->next) if ((pt->type==XML_ELEMENT_NODE)&&(!strcmp((char*)pt->name,"token")))
    {
    nbtoken++;
    if ((!strcmp((char*)find_attribute(pt->properties,"type"),"wtoken"))||(!strncmp((char*)find_attribute(pt->properties,"type"),"ponct",5)))
     {
     nonempty=True;
     sprint_word(ch,pt->children);
     ch2[0]='\0';
     sprint_word(ch2,pt->children);
     /* clean of ( and ) and * if there's anything else */
     for(i=0;(ch2[i])&&(!if_letter(ch2[i]));i++);
     if (ch2[i])
      {
      for(i=0;ch2[i];i++) if ((ch2[i]!='(')&&(ch2[i]!=')')&&(ch2[i]!='*')) fprintf(fileword,"%c",ch2[i]);
      fprintf(fileword,"\n");
      }
     else fprintf(fileword,"%s\n",ch2);
     fprintf(fileid,"%s\n",find_attribute(pt->properties,"id"));
     nbword++;
     }
    }
   if (nonempty) { fprintf(fileword,"</s>\n"); fprintf(fileid,"</s>\n"); }
   }
  }
 write_text_to_tag(cur_node->children,fileword,fileid);
 }
}

void write_tagg(FILE *filene, FILE *ftagne)
{
static char ch[10*TailleLigne];
static char chne[TailleLigne], chtag[100];
char *ptch,*ptch2,*ptid,*ptword,*ptne;
int i;

/* NE */
/*<ne id="s00001_ne0001" token="s00001_t0005" cat="pers"> Louis </ne>*/
for(chne[0]=chtag[0]='\0',i=0;fgets(ch,TailleLigne,ftagne);) if ((strncmp(ch,"<s>",3))&&(strncmp(ch,"</s>",4)))
 {
 ptid=strtok(ch," \t\n"); if (!ptid) ERREUR("bad format(11):",ch);
 ptword=strtok(NULL," \t\n"); if (!ptword) ERREUR("bad format(12):",ch);
 ptne=strtok(NULL," \t\n"); if (!ptne) ERREUR("bad format(13):",ch);
 if (strcmp(ptne,"NONE"))
  {
  if (!strncmp((char*)(ptne+strlen(ptne)-2),"_b",2)) /* new one */
   {
   i++;
   if (chne[0]) fprintf(filene,"\" cat=\"%s\"> %s </ne>\n",chtag,chne);
   chne[0]=chtag[0]='\0';
   strcpy(chne,ptword); strcpy(chtag,ptne); chtag[strlen(chtag)-2]='\0';
   fprintf(filene,"<ne id=\"ne%04d\" token=\"%s",i,ptid);
   }
  else { strcat(chne," "); strcat(chne,ptword); fprintf(filene," %s",ptid); }
  }
 else
  {
  if (chne[0]) fprintf(filene,"\" cat=\"%s\"> %s </ne>\n",chtag,chne);
  chne[0]=chtag[0]='\0';
  }
 }
if (chne[0]) fprintf(filene,"\" cat=\"%s\"> %s </ne>\n",chtag,chne);
}

/*................................................................*/

int main(int argc, char **argv)
{
char ch[TailleLigne],*dirwav,*namefile,*chdate;
xmlDoc *doc=NULL;
xmlNode *root_element;
static char *chempty=".";
int nb,itime,minuscule;
FILE *filene,*fileword,*fileid,*ftagpos,*ftagne;

/*
* this initialize the library and check potential ABI mismatches
* between the version it was compiled for and the actual shared
* library used.
*/
LIBXML_TEST_VERSION
filene=NULL;
dirwav=chempty;
chdate=NULL;
minuscule=False;
if (argc>1)
 for(nb=1;nb<argc;nb++)
  if (!strcmp(argv[nb],"-doc"))
   {
   if (nb+1==argc) ERREUR("an option must follow option:",argv[nb]);
   if (!(doc=xmlReadFile(argv[++nb],NULL,0))) ERREUR("could not parse file:",argv[nb]);
   namefile=argv[nb];
   }
  else
  if (!strcmp(argv[nb],"-ne"))
   {
   if (nb+1==argc) ERREUR("an option must follow option:",argv[nb]);
   if (!(filene=fopen(argv[++nb],"wt"))) ERREUR("can't write in:",argv[nb]);
   }
  else
  if (!strcmp(argv[nb],"-date"))
   {
   if (nb+1==argc) ERREUR("an option must follow option:",argv[nb]);
   chdate=argv[++nb];
   }
  else
  if (!strcmp(argv[nb],"-minuscule")) minuscule=True;
  else
  if (!strcmp(argv[nb],"-h"))
   {
   fprintf(stderr,"Syntax: %s [-h] -doc <file xml> -ne <output> [-minuscule] [-date <string>]\n",argv[0]);
   exit(0);
   }
  else ERREUR("unknown option:",argv[nb]);

if ((!doc)||(!filene)) ERREUR("bad syntax, check '-h'","");

/*Get the root element node */
root_element = xmlDocGetRootElement(doc);

xmlNode *ptnode;
ptnode=find_node(root_element,"Token");
fprintf(filene,"<NE ne_tagger=\"LIA_NE\" type=\"AUTO\" audio_filename=\"%s\" version=\"01\" date=\"%s\" token_filename=\"%s\">\n",
	ptnode?find_attribute(ptnode->properties,"audio_filename"):"????",chdate?chdate:"????",namefile);

/* write the corpus to tag */
itime=(int)(times(NULL));
sprintf(ch,"tmp%d.word.txt",itime);
if (!(fileword=fopen(ch,"wt"))) ERREUR("can't write in:",ch);
sprintf(ch,"tmp%d.id.txt",itime);
if (!(fileid=fopen(ch,"wt"))) ERREUR("can't write in:",ch);
write_text_to_tag(root_element,fileword,fileid);
fclose(fileword);
fclose(fileid);

/* tag it */
sprintf(ch,"$LIA_NE/script/run_ne_tagg.csh tmp%d %s",itime,minuscule?"minu":"capi");
system(ch);

/* now produce the files */
sprintf(ch,"tmp%d.id_word.ne",itime);
if (!(ftagne=fopen(ch,"rt"))) ERREUR("can't read:",ch);
write_tagg(filene,ftagne);
fclose(ftagne);

fprintf(filene,"</NE>\n"); fclose(filene);

/* clean */
sprintf(ch,"rm -f tmp%d.word.txt tmp%d.id.txt tmp%d.word.ne tmp%d.id_word.ne tmp%d.id.txt2 tmp%d.word.ne2",
	itime,itime,itime,itime,itime,itime);
system(ch);

/*free the document */
xmlFreeDoc(doc);
/*
*Free the global variables that may
*have been allocated by the parser.
*/
xmlCleanupParser();
return 0;
}
#else
int main(void)
{
fprintf(stderr, "Tree support not compiled in\n");
exit(1);
}
#endif
 
