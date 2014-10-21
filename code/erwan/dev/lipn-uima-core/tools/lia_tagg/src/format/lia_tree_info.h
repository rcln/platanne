/*  Generik info for lia_tree_avl  */

typedef struct
	{
	char *key;
	int code;
	} type_info;

int compare_info(type_info *a, type_info *b)
{
return strcmp(a->key,b->key);
}

type_info *new_type_info(char *key,int code)
{
type_info *pt;
pt=(type_info *)malloc(sizeof(type_info));
if (key) pt->key=strdup(key); else pt->key=NULL;
pt->code=code;
return pt;
}

