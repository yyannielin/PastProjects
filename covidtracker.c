#include <stdio.h>
#include <stdlib.h>
#include <string.h>

struct tree { 
   char name[50];
   struct tree* left;
   struct tree* right;
};

void printhelper(struct tree* first){ 
    if (first==NULL)return;
    printf("%s\n", first->name);
    printhelper(first->left);
    printhelper(first->right);
}

void freeHelper(struct tree* first){
    if (first==NULL)return;
    freeHelper(first->left);
    freeHelper(first->right);
    free(first);
}

void sort_and_fill(struct tree* parent, struct tree* child){

    if (parent==NULL||child==NULL)return;

    if (parent->left==NULL){
        parent->left=child;
        return;}

    int order=strcmp(child->name,parent->left->name);

    if (order>0){
        parent->right=child;
        return;
    }
    else {
        parent->right = parent->left;
        parent->left = child;
        return;
    }
}

struct tree* search(struct tree* first, char parentName[50]){
    if (first==NULL) return NULL;
    if (strcmp(first->name,parentName)==0){
        return first;
    }
    if (search(first->left,parentName)==NULL) {
        return search(first->right,parentName);}

    return search(first->left,parentName);
}

int main(int argc, char *argv[]){
    FILE* myFile;
    char scanName1[50]; //can this be used repeatedly? //is static allocation ok here?
    char scanName2[50];

    char firstName[50]="BlueDevil";
    char done[50]="DONE\n";

    myFile=fopen(argv[1],"r+");
    
    struct tree* first = (struct tree*) malloc(sizeof(struct tree));
    strcpy(first->name, firstName);
    first->left=NULL;
    first->right=NULL;

    while (1){ 

        int match=fscanf(myFile, "%s %s", scanName1, scanName2); 
        if (match!=2)break;
        char* parentName=scanName2;
        char* childName=scanName1;

        // printf("%s\n", parentName);
        // printf("%s\n", childName);

        // if (strcmp(scanName1,done)==0) break;

        struct tree* parent = first;
        parent = search(parent, parentName); //parent will point at wherever child should be added
        struct tree* child = (struct tree*) malloc(sizeof(struct tree));
        strcpy(child->name, childName);
        child->left=NULL;            
        child->right=NULL;

        sort_and_fill(parent,child);
        // printf("%s", parent->name);
    }

    printhelper(first);  

    freeHelper(first);
    
    fclose(myFile);

    return 0;
}


   




