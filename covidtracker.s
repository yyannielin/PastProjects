.data
blue_devil: .asciiz "BlueDevil\n"
done: .asciiz "DONE\n"

patient_prompt: .asciiz "Please Enter Patient: "
infecter_prompt: .asciiz "Please Enter Infecter: "
patient_buffer: .space 16 #create space in memory
infecter_buffer: .space 16
left_buffer: .space 4
right_buffer: .space 4

.text 
main:
    #create stack frame 
    addi $sp, $sp, -4
    sw $ra, 0($sp)  

#build the tree - $s0 will be the root 

li $v0, 9           #allocate memory for new record to heap
li $a0, 24          #24 bytes memory for 2 addresses and all the string
syscall             #v0 = address of allocated memory 

move $s0, $v0       #$s0 will be the root; memory space copied to $s0 

#add val to root node 

la $a1, blue_devil   
la $a0, 0($s0)

addi $sp, $sp, -4
sw $ra, 0($sp) 
jal strcpy
lw $ra, 0($sp) 
addi $sp, $sp, 4

#loading input values
#load patient (input 1) to s1, infecter (input 2) to t1
load:

#enter patient
li 	$v0, 4          #print prompt 
la	$a0, patient_prompt
syscall

li	$v0, 8          #read string 
la	$a0, patient_buffer    
li	$a1, 16         
syscall 			#patient in patient_buffer after syscall

#check if "Done" is the input 
la $a0, done
la $a1, patient_buffer

addi $sp, $sp, -8
sw $ra, 0($sp) 
sw $s0, 4($sp)
jal strcmp
lw $ra, 0($sp) 
lw $s0, 4($sp)
addi $sp, $sp, 8

move $a0, $s0
beq $v0, $0, print #enter print helper if done 

#enter infector
li 	$v0, 4           
la	$a0, infecter_prompt
syscall

li	$v0, 8          
la	$a0, infecter_buffer    
li	$a1, 16         
syscall 			#infecter in infecter_buffer after syscall

#search helper
#return parent node for patient
move $a0, $s0
la $a1, infecter_buffer

addi $sp, $sp, -4
sw $ra, 0($sp) 
jal search
lw $ra, 0($sp) 
addi $sp, $sp, 4	
move $s1, $v0		#set s1 as pointer to parent node 

#allocate memory for child node s2 and set value
li $v0, 9           
li $a0, 24          
syscall
move $s2, $v0

la $a1, patient_buffer
la $a0, 0($s2)

addi $sp, $sp, -4
sw $ra, 0($sp) 
jal strcpy
lw $ra, 0($sp)
addi $sp, $sp, 4

#fill and sort helper 
#load patient to the right place
move $a0, $s1		#set parent pointer s1(a0) and child pointer s2(a1) as argument
move $a1, $s2

addi $sp, $sp, -16
sw $ra, 0($sp) 
sw $s0, 4($sp)
sw $s1, 8($sp)
sw $s2, 12($sp)
jal fill_and_sort
lw $ra, 0($sp) 
lw $s0, 4($sp)
lw $s1, 8($sp)
lw $s2, 12($sp)
addi $sp, $sp, 16

#CLEAN BUFFERS 
la $a0, patient_buffer
addi $sp, $sp, -16
sw $ra, 0($sp) 
sw $s0, 4($sp)
sw $s1, 8($sp)
sw $s2, 12($sp)
jal strclr
lw $ra, 0($sp) 
lw $s0, 4($sp)
lw $s1, 8($sp)
lw $s2, 12($sp)
addi $sp, $sp, 16

la $a0, infecter_buffer
addi $sp, $sp, -16
sw $ra, 0($sp) 
sw $s0, 4($sp)
sw $s1, 8($sp)
sw $s2, 12($sp)
jal strclr
lw $ra, 0($sp) 
lw $s0, 4($sp)
lw $s1, 8($sp)
lw $s2, 12($sp)
addi $sp, $sp, 16

j load 

### END OF MAIN 
lw $ra, 0($sp)
addi $sp, $sp, 4
jr $ra


### HELPERS ARE BELOW 

strclr:
	lb $t0, 0($a0)
	beq $t0, $zero, done_clearing
	sb $zero, 0($a0)
	addi $a0, $a0, 1
	j strclr

	done_clearing:
	jr $ra

strcmp:
	lb $t0, 0($a0)
	lb $t1, 0($a1)

	bne $t0, $t1, done_with_strcmp_loop
	addi $a0, $a0, 1
	addi $a1, $a1, 1
	bnez $t0, strcmp
	li $v0, 0
	jr $ra
		
	done_with_strcmp_loop:
	sub $v0, $t0, $t1
	jr $ra

strcpy:
	lb $t0, 0($a1)
	beq $t0, $zero, done_copying
	sb $t0, 0($a0)
	addi $a0, $a0, 1
	addi $a1, $a1, 1
	j strcpy

	done_copying:
	jr $ra

#locate where infecter is in the tree
### a0 is root node, a1 is buffer storing infecter name
search:
	beq $a0, $0, exit 
	move $t0, $a0

	addi $sp, $sp, -16
	sw $ra, 0($sp)
	sw $a0, 4($sp)
	sw $a1, 8($sp)
	sw $t0, 12($sp)
	jal strcmp
	lw $ra, 0($sp)
	lw $a0, 4($sp)
	lw $a1, 8($sp)
	lw $t0, 12($sp)
	addi $sp, $sp, 16

	beq $v0, $0, return 
	bne $v0, $0, next

next: 
	addi $sp, $sp, -16
	sw $ra, 0($sp)
	sw $a0, 4($sp)
	sw $a1, 8($sp)
	sw $t0, 12($sp)
	jal search_left
	lw $ra, 0($sp)
	lw $a0, 4($sp)
	lw $a1, 8($sp)
	lw $t0, 12($sp)
	addi $sp, $sp, 16

	move $t2, $0
	addi $t2, $t2, 1
	beq $t1, $t2, exit 

	addi $sp, $sp, -16
	sw $ra, 0($sp)
	sw $a0, 4($sp)
	sw $a1, 8($sp)
	sw $t0, 12($sp)
	jal search_right
	lw $ra, 0($sp)
	lw $a0, 4($sp)
	lw $a1, 8($sp)
	lw $t0, 12($sp)
	addi $sp, $sp, 16
	jr $ra

search_left:
	move $t0, $a0
	lw $a0, 16($a0)

	beq $a0, $0, search_right

	addi $sp, $sp, -16
	sw $ra, 0($sp)
	sw $a0, 4($sp)
	sw $a1, 8($sp)
	sw $t0, 12($sp)
	jal search
	lw $ra, 0($sp)
	lw $a0, 4($sp)
	lw $a1, 8($sp)
	lw $t0, 12($sp)
	addi $sp, $sp, 16

	move $t2, $0
	addi $t2, $t2, 1
	beq $t1, $t2, exit 

search_right:
	move $a0, $t0
	lw $a0, 20($a0)	#go right 

	addi $sp, $sp, -16
	sw $ra, 0($sp)
	sw $a0, 4($sp)
	sw $a1, 8($sp)
	sw $t0, 12($sp)
	jal search
	lw $ra, 0($sp)
	lw $a0, 4($sp)
	lw $a1, 8($sp)
	lw $t0, 12($sp)
	addi $sp, $sp, 16
	jr $ra 

return:
	move $v0, $a0
	move $t1, $0
	addi $t1, $t1, 1 #add a signal showing completion of search
	jr $ra 

#fill tree with new nodes and sort nodes 
fill_and_sort:		
	beq $a0, $0, exit 
	beq $a1, $0, exit 

	lw $t0, 16($a0)
	beq $t0, $0, fill_left

	addi $sp, $sp, -12
	sw $ra, 0($sp)
	sw $a0, 4($sp)
	sw $a1, 8($sp)

	lw $a0, 16($a0)	#val in parent->left 

	jal strcmp
	lw $ra, 0($sp)
	lw $a0, 4($sp)
	lw $a1, 8($sp)
	addi $sp, $sp, 12

	blt $v0, $0, fill_right

	#???
	lw $t0, 16($a0)
	sw $t0, 20($a0)
	sw $a1, 16($a0)

	jr $ra 

fill_left:
	sw $a1, 16($a0)	
	jr $ra 

fill_right:
	sw $a1, 20($a0)	
	jr $ra  

#print the tree inorder 
print:
	beq $a0, $0, exit 
	move $t0, $a0

	li $v0, 4		#print root 
	la $a0, 0($a0)
	syscall 	

	move $a0, $t0

	addi $sp, $sp, -8
	sw $ra, 0($sp)
	sw $a0, 4($sp)
	lw $a0, 16($a0)	#load left node 
	jal print 
	lw $ra, 0($sp)
	lw $a0, 4($sp)
	addi $sp, $sp, 8

	addi $sp, $sp, -8
	sw $ra, 0($sp)
	sw $a0, 4($sp)
	lw $a0, 20($a0)	#load right node 
	jal print 
	lw $ra, 0($sp)
	lw $a0, 4($sp)
	addi $sp, $sp, 8

#exit for all helpers
exit:
	jr $ra 