#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include <math.h>

int logbase2(int x){
    int result=log(x)/log(2);
    return result;
}

struct block{
    int valid_bit, dirty_bit, tag, index, offset, num_accesses;
    char data[65];
};

// int find_LRU(int index, int sets, int associativity, struct block cache[sets][associativity]){
//     int way=0;
//     int least=cache[index][way].num_accesses;
//     for (int j=0;j<associativity;j++){
//         cache[index][j].num_accesses<least;
//         way=j;
//         }
//     return way;
// }

char memory[65536];

int main(int argc, char* argv[]){
    //variables from tracefile 
    char insn[6];
    char load[6]="load";
    char store[6]="store";
    int addr, access_size;

    //variables from command line 
    int cache_size, associativity, block_size;
    char cache_mode[3];
    char wt[3]="wt", wb[3]="wb";

    sscanf(argv[2], "%d", &cache_size);
    sscanf(argv[3], "%d", &associativity);
    sscanf(argv[4], "%s", cache_mode);
    sscanf(argv[5], "%d", &block_size); 

    //important variables for building memory
    cache_size=cache_size*1024;
    int sets=(cache_size/block_size)/associativity; //cache_size/block_size=num_frames
    // int offset=logbase2(block_size); //number of bits for offset 
    // int index=logbase2(sets);
    // int tag=16-offset-index; 

    //cache and memory 
    struct block cache[sets][associativity];
    // memset(memory, 0, sizeof memory);
    // printf("%s\n",memory);
    // printf("mem created\n");

    //output variables 
    char status[6], hit[6]="hit", miss[6]="miss";

    //open file and parse each line
    FILE* myFile=fopen(argv[1], "r");
    if(myFile==NULL){
        perror("couldn't open");
        return EXIT_FAILURE;
    }

    while (1){ 

        int eof=fscanf(myFile, "%s", insn);
        if (eof==EOF) break;
        addr=0;
        fscanf(myFile, "%x %d", &addr, &access_size);

        // if (strcmp(insn,load)==0){
        //     fscanf(myFile, "%x %d", &addr, &access_size);
        //     // printf("%s %x %d\n",insn,addr,access_size);    
        // }

        //parsing addr
        int blk_offset=addr%block_size;
        int blk_index=(addr/block_size)%sets;
        int blk_tag=addr/(sets*block_size);

        //printf("blk_offset ");
        //printf("%d\n",blk_offset);
        // printf("new line ");
        // printf("tag=%d\n", blk_tag);

        //scan and parse input_val for store only 
        unsigned char output_val[access_size];
        unsigned char input_val[access_size];
        int start=addr-blk_offset;

        //printf("start = %d\n", start);
        // printf("start=%d addr=%x offset=%d\n", start, addr, blk_offset);
       
        if (strcmp(insn,store)==0){
            for (int i = 0; i < access_size; i++) {
	            fscanf(myFile, "%02hhx", input_val + i);
                // printf("data = %s\n",input_val+i);
            }
            // fscanf(myFile,"%s",input_val); 
        }
        //printf("%02hhx\n",input_val[0]);
       
        //detect hit or miss 
        // printf("associativity=%d\n",associativity);
        int found=-1;
        for (int i=0; i<associativity; i++){
            // printf("i=%d ",i);
            // printf("cur tag=%d\n",cache[blk_index][i].tag);
            //hit in cache 
            //logic: if you find a block with matching tag and valid = 1, then you print that block's data if load, or write data to cache (if store & wb) or write to both cache and memory (if store & wt) 
            // if the tag doesn't match and the block is valid, you would look for the next available frame in the set to store your block in, evicting the LRU if necessary.
            // if your tag matches but the block isn't valid, that means you haven't found the block in your cache (the tag matches because of coincidence) and it's a miss -> go to main memory
          
            if(cache[blk_index][i].valid_bit!=1){
                found=i;
                //printf("found empty\n");
                break;
            }
            if (cache[blk_index][i].tag==blk_tag && cache[blk_index][i].valid_bit==1){ 
                found=-2;
                strcpy(status,hit);     
                // printf("%s\n", status);
                cache[blk_index][i].num_accesses=0;           
                if (strcmp(insn,load)==0){
                    for (int j=0;j<access_size;j++){
                        output_val[j]=cache[blk_index][i].data[j+blk_offset];
                        // printf("%02hhx\n", memory[addr+i]);
                    }
                }
                if (strcmp(insn,store)==0 && strcmp(cache_mode,wb)==0){
                    for (int j=0;j<access_size;j++){
                        cache[blk_index][i].data[blk_offset+j]=input_val[j];
                        // printf("%02hhx\n", memory[addr+i]);
                    }
                    cache[blk_index][i].dirty_bit=1;
                }
                if (strcmp(insn,store)==0 && strcmp(cache_mode,wt)==0){
                    for (int j=0;j<access_size;j++){
                        cache[blk_index][i].data[blk_offset+j]=input_val[j];
                        // printf("%02hhx\n", memory[addr+i]);
                        memory[addr+j] = input_val[j];
                    }
                }
                break;
            }
        }
        
            //after miss in cache, bring up the block to cache for load and store-wb
            // if instruction is load, load data from memory[address] 
            // if instruction is store, if wb write to cache only, and if wt (DON'T BRING UP BLOCK TO CACHE) write to mem
        if(found!=-2){
            strcpy(status,miss);
            if (strcmp(insn,load)==0){
                // printf("entered load in miss\n");
                // printf("%s\n",memory);
                // printf("%04x",addr);              
                // printf("%s",memory+addr);
                // scanf(memory+addr,"%s",output_val);
                //                     // printf("entered\n");
                //             printf("%s\n",output_val);
            
                //find LRU block; check dirty_bit (if value is 1, write data to memory); set valid_bit to 0; evict the block

                //dirty_bit logic:
                // dirty_bit is when value in cache is not the same as that in memory
                // this happens in wb when cache gets a new value but value in main memory is not updated 
                // will dirty bit never be used in wt?

                // int way=find_LRU(index,sets,associativity,cache[sets][associativity]);
                if(found==-1){
                    //printf("evicting\n");
                    int way=0;
                    int max=cache[blk_index][way].num_accesses;
                    for (int j=0;j<associativity;j++){
                        // printf("least=%d\n", least);                    
                        if (cache[blk_index][j].num_accesses>max){
                            way=j;
                            max = cache[blk_index][j].num_accesses;
                        }
                    }
                    // printf("way=%d\n", way);

                    //check LRU block dirty bit and evict LRU block
                    if (strcmp(cache_mode, wb)==0 && cache[blk_index][way].dirty_bit==1){
                        //strcpy(memory+addr,cache[blk_index][way].data);
                        int eAddress = cache[blk_index][way].tag * sets * block_size;
                        eAddress = ((int)(eAddress)/block_size) * block_size;
                        for (int i=0;i<block_size;i++){
                            memory[eAddress+i] = cache[blk_index][way].data[i];
                            cache[blk_index][way].data[i]=0;
                        }
                        cache[blk_index][way].dirty_bit=0;
                    }
                    
                    cache[blk_index][way].valid_bit=0;
                    found=way;
                }
                //find LRU block 
                
                //update everything to that in block of interest 
                //strcpy(cache[index][way].data,memory+start);

                for (int i=0;i<block_size;i++){
                    cache[blk_index][found].data[i]=memory[start+i];
                    //printf("%02hhx",cache[blk_index][way].data[i]);
                }
                // printf("block offset: %d\n", blk_offset);
                for (int i=0;i<access_size;i++){
                    output_val[i]=cache[blk_index][found].data[blk_offset+i];
                    // printf("%02hhx",output_val[i]);
                }           

                cache[blk_index][found].tag=blk_tag;
                // cache[blk_index][found].offset=blk_offset;
                cache[blk_index][found].valid_bit=1;
                cache[blk_index][found].num_accesses=0;

            }
            if (strcmp(insn,store)==0 && strcmp(cache_mode,wb)==0){
                                    // printf("entered wb mem access\n");


                //find LRU block; check dirty_bit (if value is 1, write data to memory); set valid_bit to 0; evict the block
                // int way=find_LRU(index,sets,associativity,cache[sets][associativity]);

                //find LRU block 
                if(found==-1){
                    //printf("evicting\n");

                    int way=0;
                    int max=cache[blk_index][way].num_accesses;
                    for (int j=0;j<associativity;j++){
                        // printf("least=%d\n", least);                    
                        if (cache[blk_index][j].num_accesses>max){
                            way=j;
                            max = cache[blk_index][j].num_accesses;
                        }
                    }
                    // printf("way=%d\n", way);

                    //check LRU block dirty bit and evict LRU block
                    if (strcmp(cache_mode, wb)==0 && cache[blk_index][way].dirty_bit==1){
                        //strcpy(memory+addr,cache[blk_index][way].data);
                        int eAddress = cache[blk_index][way].tag * sets * block_size;
                        eAddress = ((int)(eAddress)/block_size) * block_size;
                        for (int i=0;i<block_size;i++){
                            memory[eAddress+i] = cache[blk_index][way].data[i];
                            cache[blk_index][way].data[i]=0;
                        }
                        cache[blk_index][way].dirty_bit=0;
                    }
                    
                    cache[blk_index][way].valid_bit=0;
                    found=way;
                }
                //update everything to that in block of interest 
                //strcpy(cache[blk_index][found].data,memory+addr);
                for (int i=0;i<access_size;i++){
                    cache[blk_index][found].data[i+blk_offset]=input_val[i];
                    // printf("%02hhx\n", memory[addr+i]);
                }
                cache[blk_index][found].tag=blk_tag;
                // cache[blk_index][found].offset=blk_offset;
                cache[blk_index][found].valid_bit=1;
                cache[blk_index][found].dirty_bit=1;                   
                cache[blk_index][found].num_accesses=0;

                // store input_val to block of interest
                // strcpy(cache[blk_index][way].data,input_val);
            }
            if (strcmp(insn,store)==0 && strcmp(cache_mode,wt)==0){
                                    // printf("entered wt mem access\n");
                                    // printf("data = %s\n",input_val);
                // strcpy(memory+addr,(char*)input_val);
                for (int i=0;i<access_size;i++){
                    memory[addr+i]=input_val[i];
                    //printf("Stored: %02hhx", memory[addr+i]);
                }
                //printf("\n");
                //is wt not related with LRU so we don't have to worry about updating num_accesses?
            }
        }
    
        for (int i=0; i<associativity; i++){
          if(cache[blk_index][i].valid_bit==1){
                cache[blk_index][i].num_accesses+=1;           
            }
        }
    
        //print values 

        // printf("start = %d\n", start);
        // printf("output ");

        if (strcmp(insn,store)==0) printf("%s %04x %s\n", insn, addr, status);
        //             printf("%s %04x %s %s", insn, addr, status, output_val); 

        if (strcmp(insn,load)==0){ 
            printf("%s %04x %s ", insn, addr, status); 
            for (int i = 0; i < access_size; i++) {
                printf("%02hhx", *(output_val + i));
            }
            printf("\n");
        }

        // printf("%s\n",status);
        // break;
    }

    fclose(myFile);

    return 0; 
}
