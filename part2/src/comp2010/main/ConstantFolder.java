/*
 * IGNORING NEG, OR, REM, SHL, SHR, USHR, XOR
 * Handling NOP instructions too
 * Method ifHandler handles if instructions but if the if is
 * not true it doesn't know that it shouldn't look into it
 * so we commented it out.
 */

package comp2010.main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;

import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantPool;

import org.apache.bcel.classfile.ConstantInteger;

import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.StoreInstruction;
import org.apache.bcel.generic.ArithmeticInstruction;
import org.apache.bcel.generic.BIPUSH;
import org.apache.bcel.generic.ISTORE;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.FSTORE;
import org.apache.bcel.generic.DSTORE;
import org.apache.bcel.generic.LSTORE;
import org.apache.bcel.generic.DCMPG;
import org.apache.bcel.generic.SIPUSH;
import org.apache.bcel.generic.DCMPL;
import org.apache.bcel.generic.DCONST;
import org.apache.bcel.generic.FCMPG;
import org.apache.bcel.generic.FCMPL;
import org.apache.bcel.generic.FCONST;
import org.apache.bcel.generic.ICONST;
import org.apache.bcel.generic.LCMP;
import org.apache.bcel.generic.LCONST;
import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.IADD;
import org.apache.bcel.generic.ILOAD;
import org.apache.bcel.generic.FLOAD;
import org.apache.bcel.generic.DLOAD;
import org.apache.bcel.generic.LLOAD;
import org.apache.bcel.generic.IMUL;
import org.apache.bcel.generic.ISUB;
import org.apache.bcel.generic.IREM;
import org.apache.bcel.generic.IDIV;
import org.apache.bcel.generic.FADD;
import org.apache.bcel.generic.FMUL;
import org.apache.bcel.generic.FSUB;
import org.apache.bcel.generic.FREM;
import org.apache.bcel.generic.FDIV;
import org.apache.bcel.generic.DADD;
import org.apache.bcel.generic.DMUL;
import org.apache.bcel.generic.DSUB;
import org.apache.bcel.generic.DREM;
import org.apache.bcel.generic.DDIV;
import org.apache.bcel.generic.LADD;
import org.apache.bcel.generic.LMUL;
import org.apache.bcel.generic.LSUB;
import org.apache.bcel.generic.LREM;
import org.apache.bcel.generic.LDIV;
import org.apache.bcel.generic.DCMPG;
import org.apache.bcel.generic.DCMPL;
import org.apache.bcel.generic.FCMPG;
import org.apache.bcel.generic.FCMPL;
import org.apache.bcel.generic.LCMP;
import org.apache.bcel.generic.NOP;
import org.apache.bcel.generic.IF_ICMPEQ;
import org.apache.bcel.generic.IF_ICMPGE;
import org.apache.bcel.generic.IF_ICMPGT;
import org.apache.bcel.generic.IF_ICMPLE;
import org.apache.bcel.generic.IF_ICMPLT;
import org.apache.bcel.generic.IF_ICMPNE;
import org.apache.bcel.generic.IFNE;
import org.apache.bcel.generic.LDC;

import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.IfInstruction;

import org.apache.bcel.generic.LocalVariableInstruction;
import org.apache.bcel.generic.StackInstruction;

public class ConstantFolder {
	ClassParser parser = null;
	ClassGen gen = null;

	JavaClass original = null;
	JavaClass optimized = null;

	ConstantPoolGen myCPGen = null;

	// Keeps track of what instructions we have
	// or will be visiting. true -> needs to be visited
	// false-> shouldn't or have already been visited
	ArrayList < Boolean > instructions = new ArrayList < Boolean > ();

	public ConstantFolder(String classFilePath) {
		try {
			this.parser = new ClassParser(classFilePath);
			this.original = this.parser.parse();
			this.gen = new ClassGen(this.original);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void optimizeMethod(ClassGen cgen, ConstantPoolGen cpgen, Method method) {
		this.myCPGen = cpgen;

		// Get the Code of the method, which is a collection of bytecode instructions
		Code methodCode = method.getCode();

		// Now get the actualy bytecode data in byte array, 
		// and use it to initialise an InstructionList
		InstructionList instList = new InstructionList(methodCode.getCode());

		// Initialise a method generator with the original method as the baseline	
		MethodGen methodGen = new MethodGen(method.getAccessFlags(), method.getReturnType(), method.getArgumentTypes(), null, method.getName(), cgen.getClassName(), instList, cpgen);

		ConstantPool cp = cpgen.getConstantPool();
		// get the constants in the pool
		Constant[] constants = cp.getConstantPool();
		for (int i = 0; i < constants.length; i++) {
			if (constants[i] instanceof ConstantInteger) System.out.println(constants[i]);

		}

		InstructionHandle valueHolder;

		// Changes the iincs to :
		// bipush value, iload_i, iadd, istore_i.
		replaceINCs(instList);

		System.out.println("\n\n\n\nCode after IINCS:");
		for (InstructionHandle handle: instList.getInstructionHandles()) {
			System.out.println(handle);
		}
		System.out.println("\nTHATS ALL\n\n\n");



		System.out.println("\n\n\n\n optimising starts from here!!");
		// InstructionHandle is a wrapper for actual Instructions
		for (InstructionHandle handle = instList.getStart(); handle != null;) {
			System.out.println(handle);
			if (handle.getInstruction() instanceof StoreInstruction) {
				System.out.println("Found StoreInstruction!!!\n");
				Number lastStackValue = getLastStackPush(instList, handle);
				if (handleStoreInstructions(instList, handle, lastStackValue)) {
					InstructionHandle handleDelete = handle;
					handle = handle.getNext();
					safeInstructionDelete(instList, handleDelete);
					instList.setPositions();
				} else {
					handle = handle.getNext();
				}
			} else if (handle.getInstruction() instanceof NOP) {
				InstructionHandle handleDelete = handle;
				handle = handle.getNext();
				safeInstructionDelete(instList, handleDelete);
				instList.setPositions();
			}
			/*else if (handle.getInstruction() instanceof IfInstruction){
            	
            if (ifHandler(instList, handle)) {
                    InstructionHandle handleDelete = handle;
					handle = handle.getNext();
					safeInstructionDelete(instList, handleDelete);
					instList.setPositions();
                  }
                  else {
                    handle = handle.getNext();
                  }
          }*/

			else {
				handle = handle.getNext();
			}
		}

		// After this step we need to fold all non variable arithmetic instructions
		// foo(4+5) --> foo(9)
		/*
      for (InstructionHandle handle = instList.getStart(); handle != null;)
		{
			System.out.println(handle);
			if (handle.getInstruction() instanceof ArithmeticInstruction)
			{
				System.out.println("Found ArithmeticInstruction!!!\n");
				Number lastStackValue = getLastStackPush(instList, handle);
				if (handleStoreInstructions(instList, handle, lastStackValue)) {
					InstructionHandle handleDelete = handle;
					handle = handle.getNext();
					safeInstructionDelete(instList, handleDelete);
					instList.setPositions();
				}	
				else{
					handle = handle.getNext();
				}
			}
			else {
				handle = handle.getNext();
			}
		}
		*/

		System.out.println("\n\n\n\nThis is the whole code\n\n\n\n");
		for (InstructionHandle handle: instList.getInstructionHandles()) {
			System.out.println(handle);
		}
		System.out.println("\n\n\ndone\n\n");

	}

	/*private boolean ifHandler(InstructionList instList, InstructionHandle handle) {
      
      			if (handle.getInstruction() instanceof IF_ICMPEQ) {
		
					System.out.println("So we found an IF_ICMPEQ instruction looking for first number");
			
					InstructionHandle firstInstruction = handle.getPrev();
					while(!(stackChangingOp(firstInstruction) || firstInstruction != null)){
						firstInstruction = firstInstruction.getPrev();
					} 
					InstructionHandle secondInstruction = firstInstruction.getPrev();
					Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());
			
					System.out.println("First number found and is:" + firstNumber);

					System.out.println("First number handled looking for second one");

					while(!(stackChangingOp(secondInstruction) || secondInstruction != null)){
						secondInstruction = secondInstruction.getPrev();
					}
					Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

					System.out.println("Second number found and is:" + secondNumber);


					// delete first instruction
					if (firstNumber == null || secondNumber == null) {
						return false;
                        // break here!!!!!!!!!!!!!!!!!!!!!!!!!
					}

                  	
            	    if ((int)firstNumber == (int)secondNumber) {
                      	for (InstructionHandle handleToDelete = handle.getNext().getNext();
                             !handleToDelete.equals(((BranchInstruction)handle.getInstruction()).getTarget().getNext()); handleToDelete = handleToDelete.getNext()) {
                        	System.out.println("Deleting: " + handleToDelete.getPrev());
                        	safeInstructionDelete(instList, handleToDelete.getPrev());
                        }
            			// delete all instruction handles from handle.getNext() to handle.getInstruction().getTarget().getPrev()
                  	}
                    return true;	
				}
      
      			if (handle.getInstruction() instanceof IF_ICMPGE) {
		
					System.out.println("So we found an IF_ICMPGE instruction looking for first number");
			
					InstructionHandle firstInstruction = handle.getPrev();
					while(!(stackChangingOp(firstInstruction) || firstInstruction != null)){
						firstInstruction = firstInstruction.getPrev();
					} 
					InstructionHandle secondInstruction = firstInstruction.getPrev();
					Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());
			
					System.out.println("First number found and is:" + firstNumber);

					System.out.println("First number handled looking for second one");

					while(!(stackChangingOp(secondInstruction) || secondInstruction != null)){
						secondInstruction = secondInstruction.getPrev();
					}
					Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

					System.out.println("Second number found and is:" + secondNumber);


					// delete first instruction
					if (firstNumber == null || secondNumber == null) {
						return false;
                        // break here!!!!!!!!!!!!!!!!!!!!!!!!!
					}
                  	
            	    if ((int)firstNumber <= (int)secondNumber) {
                      	for (InstructionHandle handleToDelete = handle.getNext().getNext();
                             !handleToDelete.equals(((BranchInstruction)handle.getInstruction()).getTarget().getNext()); handleToDelete = handleToDelete.getNext()) {
                        	System.out.println("Deleting: " + handleToDelete.getPrev());
                        	safeInstructionDelete(instList, handleToDelete.getPrev());
                        }
            			// delete all instruction handles from handle.getNext() to handle.getInstruction().getTarget().getPrev()
                  	}
                    return true;	
				}
                
      			if (handle.getInstruction() instanceof IF_ICMPGT) {
		
					System.out.println("So we found an IF_ICMPGT instruction looking for first number");
			
					InstructionHandle firstInstruction = handle.getPrev();
					while(!(stackChangingOp(firstInstruction) || firstInstruction != null)){
						firstInstruction = firstInstruction.getPrev();
					} 
					InstructionHandle secondInstruction = firstInstruction.getPrev();
					Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());
			
					System.out.println("First number found and is:" + firstNumber);

					System.out.println("First number handled looking for second one");

					while(!(stackChangingOp(secondInstruction) || secondInstruction != null)){
						secondInstruction = secondInstruction.getPrev();
					}
					Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

					System.out.println("Second number found and is:" + secondNumber);


					// delete first instruction
					if (firstNumber == null || secondNumber == null) {
						return false;
                        // break here!!!!!!!!!!!!!!!!!!!!!!!!!
					}
                  	
            	    if ((int)firstNumber < (int)secondNumber) {
                      	for (InstructionHandle handleToDelete = handle.getNext().getNext();
                             !handleToDelete.equals(((BranchInstruction)handle.getInstruction()).getTarget().getNext()); handleToDelete = handleToDelete.getNext()) {
                        	System.out.println("Deleting: " + handleToDelete.getPrev());
                        	safeInstructionDelete(instList, handleToDelete.getPrev());
                        }
            			// delete all instruction handles from handle.getNext() to handle.getInstruction().getTarget().getPrev()
                  	}
                    return true;	
				}
      
      			if (handle.getInstruction() instanceof IF_ICMPLE) {
		
					System.out.println("So we found an IF_ICMPLE instruction looking for first number");
			
					InstructionHandle firstInstruction = handle.getPrev();
					while(!(stackChangingOp(firstInstruction) || firstInstruction != null)){
						firstInstruction = firstInstruction.getPrev();
					} 
					InstructionHandle secondInstruction = firstInstruction.getPrev();
					Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());
			
					System.out.println("First number found and is:" + firstNumber);

					System.out.println("First number handled looking for second one");

					while(!(stackChangingOp(secondInstruction) || secondInstruction != null)){
						secondInstruction = secondInstruction.getPrev();
					}
					Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

					System.out.println("Second number found and is:" + secondNumber);


					// delete first instruction
					if (firstNumber == null || secondNumber == null) {
						return false;
                        // break here!!!!!!!!!!!!!!!!!!!!!!!!!
					}
                  	
            	    if ((int)firstNumber >= (int)secondNumber) {
                      	for (InstructionHandle handleToDelete = handle.getNext().getNext();
                             !handleToDelete.equals(((BranchInstruction)handle.getInstruction()).getTarget().getNext()); handleToDelete = handleToDelete.getNext()) {
                        	System.out.println("Deleting: " + handleToDelete.getPrev());
                        	safeInstructionDelete(instList, handleToDelete.getPrev());
                        }
            			// delete all instruction handles from handle.getNext() to handle.getInstruction().getTarget().getPrev()
                  	}
                    return true;	
				}
      
      			if (handle.getInstruction() instanceof IF_ICMPLT) {
		
					System.out.println("So we found an IF_ICMPGT instruction looking for first number");
			
					InstructionHandle firstInstruction = handle.getPrev();
					while(!(stackChangingOp(firstInstruction) || firstInstruction != null)){
						firstInstruction = firstInstruction.getPrev();
					} 
					InstructionHandle secondInstruction = firstInstruction.getPrev();
					Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());
			
					System.out.println("First number found and is:" + firstNumber);

					System.out.println("First number handled looking for second one");

					while(!(stackChangingOp(secondInstruction) || secondInstruction != null)){
						secondInstruction = secondInstruction.getPrev();
					}
					Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

					System.out.println("Second number found and is:" + secondNumber);


					// delete first instruction
					if (firstNumber == null || secondNumber == null) {
						return false;
                        // break here!!!!!!!!!!!!!!!!!!!!!!!!!
					}
                  	
            	    if ((int)firstNumber > (int)secondNumber) {
                      	for (InstructionHandle handleToDelete = handle.getNext().getNext();
                             !handleToDelete.equals(((BranchInstruction)handle.getInstruction()).getTarget().getNext()); handleToDelete = handleToDelete.getNext()) {
                        	System.out.println("Deleting: " + handleToDelete.getPrev());
                        	safeInstructionDelete(instList, handleToDelete.getPrev());
                        }
            			// delete all instruction handles from handle.getNext() to handle.getInstruction().getTarget().getPrev()
                  	}
                    return true;	
				}
      
      			if (handle.getInstruction() instanceof IF_ICMPNE) {
		
					System.out.println("So we found an IF_ICMPNE instruction looking for first number");
			
					InstructionHandle firstInstruction = handle.getPrev();
					while(!(stackChangingOp(firstInstruction) || firstInstruction != null)){
						firstInstruction = firstInstruction.getPrev();
					} 
					InstructionHandle secondInstruction = firstInstruction.getPrev();
					Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());
			
					System.out.println("First number found and is:" + firstNumber);

					System.out.println("First number handled looking for second one");

					while(!(stackChangingOp(secondInstruction) || secondInstruction != null)){
						secondInstruction = secondInstruction.getPrev();
					}
					Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

					System.out.println("Second number found and is:" + secondNumber);


					// delete first instruction
					if (firstNumber == null || secondNumber == null) {
						return false;
                        // break here!!!!!!!!!!!!!!!!!!!!!!!!!
					}
                  	
            	    if ((int)firstNumber != (int)secondNumber) {
                      	for (InstructionHandle handleToDelete = handle.getNext().getNext();
                             !handleToDelete.equals(((BranchInstruction)handle.getInstruction()).getTarget().getNext()); handleToDelete = handleToDelete.getNext()) {
                        	System.out.println("Deleting: " + handleToDelete.getPrev());
                        	safeInstructionDelete(instList, handleToDelete.getPrev());
                        }
            			// delete all instruction handles from handle.getNext() to handle.getInstruction().getTarget().getPrev()
                  	}
                    return true;	
				}
      
      			if (handle.getInstruction() instanceof IFNE) {
		
					System.out.println("So we found an IFNE instruction looking for first number");
			
					InstructionHandle firstInstruction = handle.getPrev();
					while(!(stackChangingOp(firstInstruction) || firstInstruction != null)){
						firstInstruction = firstInstruction.getPrev();
					} 
					InstructionHandle secondInstruction = firstInstruction.getPrev();
					Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());
			
					System.out.println("First number found and is:" + firstNumber);

					Number secondNumber = 0;

					System.out.println("Second number \"found\" and is:" + secondNumber);

					// delete first instruction
					if (firstNumber == null || secondNumber == null) {
						return false;
                        // break here!!!!!!!!!!!!!!!!!!!!!!!!!
					}
                  	
            	    if ((int)firstNumber != (int)secondNumber) {
                      	for (InstructionHandle handleToDelete = handle.getNext().getNext();
                             !handleToDelete.equals(((BranchInstruction)handle.getInstruction()).getTarget().getNext()); handleToDelete = handleToDelete.getNext()) {
                        	System.out.println("Deleting: " + handleToDelete.getPrev());
                        	safeInstructionDelete(instList, handleToDelete.getPrev());
                        }
            			// delete all instruction handles from handle.getNext() to handle.getInstruction().getTarget().getPrev()
                  	}
                    return true;	
				}
      
      			if (handle.getInstruction() instanceof IFEQ) {
		
					System.out.println("So we found an IFEQ instruction looking for first number");
			
					InstructionHandle firstInstruction = handle.getPrev();
					while(!(stackChangingOp(firstInstruction) || firstInstruction != null)){
						firstInstruction = firstInstruction.getPrev();
					} 
					InstructionHandle secondInstruction = firstInstruction.getPrev();
					Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());
			
					System.out.println("First number found and is:" + firstNumber);

					Number secondNumber = 0;

					System.out.println("Second number \"found\" and is:" + secondNumber);

					// delete first instruction
					if (firstNumber == null || secondNumber == null) {
						return false;
                        // break here!!!!!!!!!!!!!!!!!!!!!!!!!
					}
                  	
            	    if ((int)firstNumber == (int)secondNumber) {
                      	for (InstructionHandle handleToDelete = handle.getNext().getNext();
                             !handleToDelete.equals(((BranchInstruction)handle.getInstruction()).getTarget().getNext()); handleToDelete = handleToDelete.getNext()) {
                        	System.out.println("Deleting: " + handleToDelete.getPrev());
                        	safeInstructionDelete(instList, handleToDelete.getPrev());
                        }
            			// delete all instruction handles from handle.getNext() to handle.getInstruction().getTarget().getPrev()
                  	}
                    return true;	
				}

    	return false;
    }*/

	// returns true if a computable number is being stored in the stack
	private boolean handleStoreInstructions(InstructionList instList, InstructionHandle handle, Number lastStackValue) {

		if (handle.getInstruction() instanceof ISTORE && lastStackValue != null) {
			int value = (int) lastStackValue;
			int istoreIndex = ((ISTORE) handle.getInstruction()).getIndex();
			InstructionHandle handleNow = handle.getNext();
			int constantIndex = 0;

			if (value > 127 || value < -128) {
				constantIndex = myCPGen.addInteger((int) value);
				// put sth in the constant pool 
			}

			System.out.println("STATUS : looking for iloads");

			while (handleNow != null && !(handleNow.getInstruction() instanceof ISTORE && ((ISTORE) handle.getInstruction()).getIndex() == istoreIndex && handle.getInstruction().getOpcode() == handleNow.getInstruction().getOpcode())) {

				System.out.print("looking at instruction: ");
				System.out.println(handleNow);

				if (handleNow.getInstruction() instanceof ILOAD && ((ILOAD) handleNow.getInstruction()).getIndex() == istoreIndex) {
					System.out.println("the above instruction is a load and will be changed to bipush");
					if (value > 127 || value < -128) {
						instList.insert(handleNow, new LDC(constantIndex));
						instList.setPositions();
						// insert the ldc we defined a few lines above
					} else {
						instList.insert(handleNow, new BIPUSH((byte) value));
						instList.setPositions();
					}

					try {
						handleNow = handleNow.getNext();
						InstructionHandle handleDelete = handleNow.getPrev();
						instList.redirectBranches(handleDelete, handleDelete.getPrev());
						instList.delete(handleDelete);
						instList.setPositions();
					} catch (Exception e) {
						//do nothing
					}
					System.out.println("This is how it looks now");
					for (InstructionHandle hand: instList.getInstructionHandles()) {
						System.out.println(hand);
					}
				} else {
					handleNow = handleNow.getNext();
				}

			}
			System.out.println(handle);
			System.out.println("found corresponding i store and giong out ");
			return true;
		} else if (handle.getInstruction() instanceof FSTORE && lastStackValue != null) {
			float value = (float) lastStackValue;
			int fstoreIndex = ((FSTORE) handle.getInstruction()).getIndex();
			InstructionHandle handleNow = handle.getNext();

			System.out.println("STATUS : looking for floads");

			while (handleNow != null && !(handleNow.getInstruction() instanceof FSTORE && ((FSTORE) handle.getInstruction()).getIndex() == fstoreIndex && handle.getInstruction().getOpcode() == handleNow.getInstruction().getOpcode())) {

				System.out.print("looking at instruction: ");
				System.out.println(handleNow);

				if (handleNow.getInstruction() instanceof FLOAD && ((FLOAD) handleNow.getInstruction()).getIndex() == fstoreIndex) {
					System.out.println("the above instruction is a load and will be changed to bipush");
					instList.insert(handleNow, new BIPUSH((byte) value));
					instList.setPositions();
					try {
						handleNow = handleNow.getNext();
						InstructionHandle handleDelete = handleNow.getPrev();
						instList.redirectBranches(handleDelete, handleDelete.getPrev());
						instList.delete(handleDelete);
						instList.setPositions();
					} catch (Exception e) {
						//do nothing
					}
					System.out.println("This is how it looks now");
					for (InstructionHandle hand: instList.getInstructionHandles()) {
						System.out.println(hand);
					}
				} else {
					handleNow = handleNow.getNext();
				}

			}
			System.out.println(handle);
			System.out.println("found corresponding f store and giong out ");
			return true;
		} else if (handle.getInstruction() instanceof DSTORE) {} else if (handle.getInstruction() instanceof LSTORE) {} else if (handle.getInstruction() instanceof ASTORE) {} else {
			System.out.println("INVALID INSTRUCTION");
		}
		return false;
	}

	private Number getLastStackPush(InstructionList instList, InstructionHandle handle) {
		InstructionHandle lastStackOp = handle;
		do {
			lastStackOp = lastStackOp.getPrev();
		} while (!(stackChangingOp(lastStackOp) || lastStackOp != null));


		System.out.println("Previous Stack operation was : ");
		System.out.println(lastStackOp);

		if (lastStackOp.getInstruction() instanceof BIPUSH) {
			Number value = ((BIPUSH) lastStackOp.getInstruction()).getValue();
			safeInstructionDelete(instList, lastStackOp);
			return value;
		} else if (lastStackOp.getInstruction() instanceof SIPUSH) {
			Number value = ((SIPUSH) lastStackOp.getInstruction()).getValue();
			safeInstructionDelete(instList, lastStackOp);
			return value;
		} else if (lastStackOp.getInstruction() instanceof ICONST) {
			Number value = ((ICONST) lastStackOp.getInstruction()).getValue();
			safeInstructionDelete(instList, lastStackOp);
			return value;
		} else if (lastStackOp.getInstruction() instanceof DCONST) {
			Number value = ((DCONST) lastStackOp.getInstruction()).getValue();
			safeInstructionDelete(instList, lastStackOp);
			return value;
		} else if (lastStackOp.getInstruction() instanceof FCONST) {
			Number value = ((FCONST) lastStackOp.getInstruction()).getValue();
			safeInstructionDelete(instList, lastStackOp);
			return value;
		} else if (lastStackOp.getInstruction() instanceof LCONST) {
			Number value = ((LCONST) lastStackOp.getInstruction()).getValue();
			safeInstructionDelete(instList, lastStackOp);
			return value;
		} else if (lastStackOp.getInstruction() instanceof IADD) {

			System.out.println("So we found an ADD instruction looking for first number");

			InstructionHandle firstInstruction = lastStackOp.getPrev();
			while (!(stackChangingOp(firstInstruction) || firstInstruction != null)) {
				firstInstruction = firstInstruction.getPrev();
			}
			InstructionHandle secondInstruction = firstInstruction.getPrev();
			Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());

			System.out.println("First number found and is:" + firstNumber);

			System.out.println("First number handled looking for second one");

			while (!(stackChangingOp(secondInstruction) || secondInstruction != null)) {
				System.out.println("SAM");
				secondInstruction = secondInstruction.getPrev();
			}
			Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

			System.out.println("second number found and is:" + secondNumber);


			// delete first instruction
			if (firstNumber == null || secondNumber == null) {
				return null;
			}

			safeInstructionDelete(instList, lastStackOp);
			return ((int) firstNumber + (int) secondNumber);
		} else if (lastStackOp.getInstruction() instanceof IMUL) {

			System.out.println("So we found an MUL instruction looking for first number");

			InstructionHandle firstInstruction = lastStackOp.getPrev();
			while (!(stackChangingOp(firstInstruction) || firstInstruction != null)) {
				firstInstruction = firstInstruction.getPrev();
			}
			InstructionHandle secondInstruction = firstInstruction.getPrev();
			Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());

			System.out.println("First number found and is:" + firstNumber);

			System.out.println("First number handled looking for second one");

			while (!(stackChangingOp(secondInstruction) || secondInstruction != null)) {
				System.out.println("SAM");
				secondInstruction = secondInstruction.getPrev();
			}
			Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

			System.out.println("second number found and is:" + secondNumber);


			// delete first instruction
			if (firstNumber == null || secondNumber == null) {
				return null;
			}

			safeInstructionDelete(instList, lastStackOp);
			return ((int) firstNumber * (int) secondNumber);
		} else if (lastStackOp.getInstruction() instanceof IDIV) {

			System.out.println("So we found an DIV instruction looking for first number");

			InstructionHandle firstInstruction = lastStackOp.getPrev();
			while (!(stackChangingOp(firstInstruction) || firstInstruction != null)) {
				firstInstruction = firstInstruction.getPrev();
			}
			InstructionHandle secondInstruction = firstInstruction.getPrev();
			Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());

			System.out.println("First number found and is:" + firstNumber);

			System.out.println("First number handled looking for second one");

			while (!(stackChangingOp(secondInstruction) || secondInstruction != null)) {
				System.out.println("SAM");
				secondInstruction = secondInstruction.getPrev();
			}
			Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

			System.out.println("second number found and is:" + secondNumber);


			// delete first instruction
			if (firstNumber == null || secondNumber == null) {
				return null;
			}

			safeInstructionDelete(instList, lastStackOp);
			return ((int) secondNumber / (int) firstNumber);
		} else if (lastStackOp.getInstruction() instanceof ISUB) {

			System.out.println("So we found an SUB instruction looking for first number");

			InstructionHandle firstInstruction = lastStackOp.getPrev();
			while (!(stackChangingOp(firstInstruction) || firstInstruction != null)) {
				firstInstruction = firstInstruction.getPrev();
			}
			InstructionHandle secondInstruction = firstInstruction.getPrev();
			Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());

			System.out.println("First number found and is:" + firstNumber);

			System.out.println("First number handled looking for second one");

			while (!(stackChangingOp(secondInstruction) || secondInstruction != null)) {
				System.out.println("SAM");
				secondInstruction = secondInstruction.getPrev();
			}
			Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

			System.out.println("second number found and is:" + secondNumber);


			// delete first instruction
			if (firstNumber == null || secondNumber == null) {
				return null;
			}

			safeInstructionDelete(instList, lastStackOp);
			return ((int) secondNumber - (int) firstNumber);
		} else if (lastStackOp.getInstruction() instanceof IREM) {

			System.out.println("So we found an REM instruction looking for first number");

			InstructionHandle firstInstruction = lastStackOp.getPrev();
			while (!(stackChangingOp(firstInstruction) || firstInstruction != null)) {
				firstInstruction = firstInstruction.getPrev();
			}
			InstructionHandle secondInstruction = firstInstruction.getPrev();
			Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());

			System.out.println("First number found and is:" + firstNumber);

			System.out.println("First number handled looking for second one");

			while (!(stackChangingOp(secondInstruction) || secondInstruction != null)) {
				System.out.println("SAM");
				secondInstruction = secondInstruction.getPrev();
			}
			Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

			System.out.println("second number found and is:" + secondNumber);
          
			// delete first instruction
			if (firstNumber == null || secondNumber == null) {
				return null;
			}

			safeInstructionDelete(instList, lastStackOp);
			return ((int) secondNumber % (int) firstNumber);
		} else if (lastStackOp.getInstruction() instanceof LADD) {

			System.out.println("So we found an ADD instruction looking for first number");

			InstructionHandle firstInstruction = lastStackOp.getPrev();
			while (!(stackChangingOp(firstInstruction) || firstInstruction != null)) {
				firstInstruction = firstInstruction.getPrev();
			}
			InstructionHandle secondInstruction = firstInstruction.getPrev();
			Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());

			System.out.println("First number found and is:" + firstNumber);

			System.out.println("First number handled looking for second one");

			while (!(stackChangingOp(secondInstruction) || secondInstruction != null)) {
				System.out.println("SAM");
				secondInstruction = secondInstruction.getPrev();
			}
			Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

			System.out.println("second number found and is:" + secondNumber);

			// delete first instruction
			if (firstNumber == null || secondNumber == null) {
				return null;
			}

			safeInstructionDelete(instList, lastStackOp);
			return ((long) firstNumber + (long) secondNumber);
		} else if (lastStackOp.getInstruction() instanceof LMUL) {

			System.out.println("So we found an MUL instruction looking for first number");

			InstructionHandle firstInstruction = lastStackOp.getPrev();
			while (!(stackChangingOp(firstInstruction) || firstInstruction != null)) {
				firstInstruction = firstInstruction.getPrev();
			}
			InstructionHandle secondInstruction = firstInstruction.getPrev();
			Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());

			System.out.println("First number found and is:" + firstNumber);

			System.out.println("First number handled looking for second one");

			while (!(stackChangingOp(secondInstruction) || secondInstruction != null)) {
				System.out.println("SAM");
				secondInstruction = secondInstruction.getPrev();
			}
			Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

			System.out.println("second number found and is:" + secondNumber);

			// delete first instruction
			if (firstNumber == null || secondNumber == null) {
				return null;
			}

			safeInstructionDelete(instList, lastStackOp);
			return ((long) firstNumber * (long) secondNumber);
		} else if (lastStackOp.getInstruction() instanceof LDIV) {

			System.out.println("So we found an DIV instruction looking for first number");

			InstructionHandle firstInstruction = lastStackOp.getPrev();
			while (!(stackChangingOp(firstInstruction) || firstInstruction != null)) {
				firstInstruction = firstInstruction.getPrev();
			}
			InstructionHandle secondInstruction = firstInstruction.getPrev();
			Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());

			System.out.println("First number found and is:" + firstNumber);

			System.out.println("First number handled looking for second one");

			while (!(stackChangingOp(secondInstruction) || secondInstruction != null)) {
				System.out.println("SAM");
				secondInstruction = secondInstruction.getPrev();
			}
			Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

			System.out.println("second number found and is:" + secondNumber);

			// delete first instruction
			if (firstNumber == null || secondNumber == null) {
				return null;
			}

			safeInstructionDelete(instList, lastStackOp);
			return ((long) secondNumber / (long) firstNumber);
		} else if (lastStackOp.getInstruction() instanceof LSUB) {

			System.out.println("So we found an SUB instruction looking for first number");

			InstructionHandle firstInstruction = lastStackOp.getPrev();
			while (!(stackChangingOp(firstInstruction) || firstInstruction != null)) {
				firstInstruction = firstInstruction.getPrev();
			}
			InstructionHandle secondInstruction = firstInstruction.getPrev();
			Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());

			System.out.println("First number found and is:" + firstNumber);

			System.out.println("First number handled looking for second one");

			while (!(stackChangingOp(secondInstruction) || secondInstruction != null)) {
				System.out.println("SAM");
				secondInstruction = secondInstruction.getPrev();
			}
			Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

			System.out.println("second number found and is:" + secondNumber);

			// delete first instruction
			if (firstNumber == null || secondNumber == null) {
				return null;
			}

			safeInstructionDelete(instList, lastStackOp);
			return ((long) secondNumber - (long) firstNumber);
		} else if (lastStackOp.getInstruction() instanceof LREM) {

			System.out.println("So we found an REM instruction looking for first number");

			InstructionHandle firstInstruction = lastStackOp.getPrev();
			while (!(stackChangingOp(firstInstruction) || firstInstruction != null)) {
				firstInstruction = firstInstruction.getPrev();
			}
			InstructionHandle secondInstruction = firstInstruction.getPrev();
			Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());

			System.out.println("First number found and is:" + firstNumber);

			System.out.println("First number handled looking for second one");

			while (!(stackChangingOp(secondInstruction) || secondInstruction != null)) {
				System.out.println("SAM");
				secondInstruction = secondInstruction.getPrev();
			}
			Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

			System.out.println("second number found and is:" + secondNumber);

			// delete first instruction
			if (firstNumber == null || secondNumber == null) {
				return null;
			}

			safeInstructionDelete(instList, lastStackOp);
			return ((long) secondNumber % (long) firstNumber);
		} else if (lastStackOp.getInstruction() instanceof FADD) {

			System.out.println("So we found an ADD instruction looking for first number");

			InstructionHandle firstInstruction = lastStackOp.getPrev();
			while (!(stackChangingOp(firstInstruction) || firstInstruction != null)) {
				firstInstruction = firstInstruction.getPrev();
			}
			InstructionHandle secondInstruction = firstInstruction.getPrev();
			Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());

			System.out.println("First number found and is:" + firstNumber);

			System.out.println("First number handled looking for second one");

			while (!(stackChangingOp(secondInstruction) || secondInstruction != null)) {
				System.out.println("SAM");
				secondInstruction = secondInstruction.getPrev();
			}
			Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

			System.out.println("second number found and is:" + secondNumber);

			// delete first instruction
			if (firstNumber == null || secondNumber == null) {
				return null;
			}

			safeInstructionDelete(instList, lastStackOp);
			return ((float) firstNumber + (float) secondNumber);
		} else if (lastStackOp.getInstruction() instanceof FMUL) {

			System.out.println("So we found an MUL instruction looking for first number");

			InstructionHandle firstInstruction = lastStackOp.getPrev();
			while (!(stackChangingOp(firstInstruction) || firstInstruction != null)) {
				firstInstruction = firstInstruction.getPrev();
			}
			InstructionHandle secondInstruction = firstInstruction.getPrev();
			Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());

			System.out.println("First number found and is:" + firstNumber);

			System.out.println("First number handled looking for second one");

			while (!(stackChangingOp(secondInstruction) || secondInstruction != null)) {
				System.out.println("SAM");
				secondInstruction = secondInstruction.getPrev();
			}
			Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

			System.out.println("second number found and is:" + secondNumber);

			// delete first instruction
			if (firstNumber == null || secondNumber == null) {
				return null;
			}

			safeInstructionDelete(instList, lastStackOp);
			return ((float) firstNumber * (float) secondNumber);
		} else if (lastStackOp.getInstruction() instanceof FDIV) {

			System.out.println("So we found an DIV instruction looking for first number");

			InstructionHandle firstInstruction = lastStackOp.getPrev();
			while (!(stackChangingOp(firstInstruction) || firstInstruction != null)) {
				firstInstruction = firstInstruction.getPrev();
			}
			InstructionHandle secondInstruction = firstInstruction.getPrev();
			Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());

			System.out.println("First number found and is:" + firstNumber);

			System.out.println("First number handled looking for second one");

			while (!(stackChangingOp(secondInstruction) || secondInstruction != null)) {
				System.out.println("SAM");
				secondInstruction = secondInstruction.getPrev();
			}
			Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

			System.out.println("second number found and is:" + secondNumber);

			// delete first instruction
			if (firstNumber == null || secondNumber == null) {
				return null;
			}

			safeInstructionDelete(instList, lastStackOp);
			return ((float) secondNumber / (float) firstNumber);
		} else if (lastStackOp.getInstruction() instanceof FSUB) {

			System.out.println("So we found an SUB instruction looking for first number");

			InstructionHandle firstInstruction = lastStackOp.getPrev();
			while (!(stackChangingOp(firstInstruction) || firstInstruction != null)) {
				firstInstruction = firstInstruction.getPrev();
			}
			InstructionHandle secondInstruction = firstInstruction.getPrev();
			Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());

			System.out.println("First number found and is:" + firstNumber);

			System.out.println("First number handled looking for second one");

			while (!(stackChangingOp(secondInstruction) || secondInstruction != null)) {
				System.out.println("SAM");
				secondInstruction = secondInstruction.getPrev();
			}
			Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

			System.out.println("second number found and is:" + secondNumber);

			// delete first instruction
			if (firstNumber == null || secondNumber == null) {
				return null;
			}

			safeInstructionDelete(instList, lastStackOp);
			return ((float) secondNumber - (float) firstNumber);
		} else if (lastStackOp.getInstruction() instanceof FREM) {

			System.out.println("So we found an REM instruction looking for first number");

			InstructionHandle firstInstruction = lastStackOp.getPrev();
			while (!(stackChangingOp(firstInstruction) || firstInstruction != null)) {
				firstInstruction = firstInstruction.getPrev();
			}
			InstructionHandle secondInstruction = firstInstruction.getPrev();
			Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());

			System.out.println("First number found and is:" + firstNumber);

			System.out.println("First number handled looking for second one");

			while (!(stackChangingOp(secondInstruction) || secondInstruction != null)) {
				System.out.println("SAM");
				secondInstruction = secondInstruction.getPrev();
			}
			Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

			System.out.println("second number found and is:" + secondNumber);

			// delete first instruction
			if (firstNumber == null || secondNumber == null) {
				return null;
			}

			safeInstructionDelete(instList, lastStackOp);
			return ((float) secondNumber % (float) firstNumber);
		} else if (lastStackOp.getInstruction() instanceof DADD) {

			System.out.println("So we found an ADD instruction looking for first number");

			InstructionHandle firstInstruction = lastStackOp.getPrev();
			while (!(stackChangingOp(firstInstruction) || firstInstruction != null)) {
				firstInstruction = firstInstruction.getPrev();
			}
			InstructionHandle secondInstruction = firstInstruction.getPrev();
			Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());

			System.out.println("First number found and is:" + firstNumber);

			System.out.println("First number handled looking for second one");

			while (!(stackChangingOp(secondInstruction) || secondInstruction != null)) {
				System.out.println("SAM");
				secondInstruction = secondInstruction.getPrev();
			}
			Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

			System.out.println("second number found and is:" + secondNumber);

			// delete first instruction
			if (firstNumber == null || secondNumber == null) {
				return null;
			}

			safeInstructionDelete(instList, lastStackOp);
			return ((double) firstNumber + (double) secondNumber);
		} else if (lastStackOp.getInstruction() instanceof DMUL) {

			System.out.println("So we found an MUL instruction looking for first number");

			InstructionHandle firstInstruction = lastStackOp.getPrev();
			while (!(stackChangingOp(firstInstruction) || firstInstruction != null)) {
				firstInstruction = firstInstruction.getPrev();
			}
			InstructionHandle secondInstruction = firstInstruction.getPrev();
			Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());

			System.out.println("First number found and is:" + firstNumber);

			System.out.println("First number handled looking for second one");

			while (!(stackChangingOp(secondInstruction) || secondInstruction != null)) {
				System.out.println("SAM");
				secondInstruction = secondInstruction.getPrev();
			}
			Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

			System.out.println("second number found and is:" + secondNumber);

			// delete first instruction
			if (firstNumber == null || secondNumber == null) {
				return null;
			}

			safeInstructionDelete(instList, lastStackOp);
			return ((double) firstNumber * (double) secondNumber);
		} else if (lastStackOp.getInstruction() instanceof DDIV) {

			System.out.println("So we found an DIV instruction looking for first number");

			InstructionHandle firstInstruction = lastStackOp.getPrev();
			while (!(stackChangingOp(firstInstruction) || firstInstruction != null)) {
				firstInstruction = firstInstruction.getPrev();
			}
			InstructionHandle secondInstruction = firstInstruction.getPrev();
			Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());

			System.out.println("First number found and is:" + firstNumber);

			System.out.println("First number handled looking for second one");

			while (!(stackChangingOp(secondInstruction) || secondInstruction != null)) {
				System.out.println("SAM");
				secondInstruction = secondInstruction.getPrev();
			}
			Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

			System.out.println("second number found and is:" + secondNumber);

			// delete first instruction
			if (firstNumber == null || secondNumber == null) {
				return null;
			}

			safeInstructionDelete(instList, lastStackOp);
			return ((double) secondNumber / (double) firstNumber);
		} else if (lastStackOp.getInstruction() instanceof DSUB) {

			System.out.println("So we found an SUB instruction looking for first number");

			InstructionHandle firstInstruction = lastStackOp.getPrev();
			while (!(stackChangingOp(firstInstruction) || firstInstruction != null)) {
				firstInstruction = firstInstruction.getPrev();
			}
			InstructionHandle secondInstruction = firstInstruction.getPrev();
			Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());

			System.out.println("First number found and is:" + firstNumber);

			System.out.println("First number handled looking for second one");

			while (!(stackChangingOp(secondInstruction) || secondInstruction != null)) {
				System.out.println("SAM");
				secondInstruction = secondInstruction.getPrev();
			}
			Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

			System.out.println("second number found and is:" + secondNumber);

			// delete first instruction
			if (firstNumber == null || secondNumber == null) {
				return null;
			}

			safeInstructionDelete(instList, lastStackOp);
			return ((double) secondNumber - (double) firstNumber);
		} else if (lastStackOp.getInstruction() instanceof DREM) {

			System.out.println("So we found an REM instruction looking for first number");

			InstructionHandle firstInstruction = lastStackOp.getPrev();
			while (!(stackChangingOp(firstInstruction) || firstInstruction != null)) {
				firstInstruction = firstInstruction.getPrev();
			}
			InstructionHandle secondInstruction = firstInstruction.getPrev();
			Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());

			System.out.println("First number found and is:" + firstNumber);

			System.out.println("First number handled looking for second one");

			while (!(stackChangingOp(secondInstruction) || secondInstruction != null)) {
				System.out.println("SAM");
				secondInstruction = secondInstruction.getPrev();
			}
			Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

			System.out.println("second number found and is:" + secondNumber);

			// delete first instruction
			if (firstNumber == null || secondNumber == null) {
				return null;
			}

			safeInstructionDelete(instList, lastStackOp);
			return ((double) secondNumber % (double) firstNumber);
		} else if (lastStackOp.getInstruction() instanceof DCMPG) {

			System.out.println("So we found an CMPG instruction looking for first number");

			InstructionHandle firstInstruction = lastStackOp.getPrev();
			while (!(stackChangingOp(firstInstruction) || firstInstruction != null)) {
				firstInstruction = firstInstruction.getPrev();
			}
			InstructionHandle secondInstruction = firstInstruction.getPrev();
			Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());

			System.out.println("First number found and is:" + firstNumber);

			System.out.println("First number handled looking for second one");

			while (!(stackChangingOp(secondInstruction) || secondInstruction != null)) {
				System.out.println("SAM");
				secondInstruction = secondInstruction.getPrev();
			}
			Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

			System.out.println("second number found and is:" + secondNumber);

			// delete first instruction
			if (firstNumber == null || secondNumber == null) {
				return null;
			}

			safeInstructionDelete(instList, lastStackOp);
			if ((double) secondNumber == (double) firstNumber) {
				return 0;
			} else if ((double) secondNumber > (double) firstNumber) {
				return 1;
			} else {
				return -1;
			}
		} else if (lastStackOp.getInstruction() instanceof DCMPL) {

			System.out.println("So we found an CMPL instruction looking for first number");

			InstructionHandle firstInstruction = lastStackOp.getPrev();
			while (!(stackChangingOp(firstInstruction) || firstInstruction != null)) {
				firstInstruction = firstInstruction.getPrev();
			}
			InstructionHandle secondInstruction = firstInstruction.getPrev();
			Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());

			System.out.println("First number found and is:" + firstNumber);

			System.out.println("First number handled looking for second one");

			while (!(stackChangingOp(secondInstruction) || secondInstruction != null)) {
				System.out.println("SAM");
				secondInstruction = secondInstruction.getPrev();
			}
			Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

			System.out.println("second number found and is:" + secondNumber);

			// delete first instruction
			if (firstNumber == null || secondNumber == null) {
				return null;
			}

			safeInstructionDelete(instList, lastStackOp);
			if ((double) secondNumber == (double) firstNumber) {
				return 0;
			} else if ((double) secondNumber < (double) firstNumber) {
				return 1;
			} else {
				return -1;
			}
		} else if (lastStackOp.getInstruction() instanceof LCMP) {

			System.out.println("So we found an CMP instruction looking for first number");

			InstructionHandle firstInstruction = lastStackOp.getPrev();
			while (!(stackChangingOp(firstInstruction) || firstInstruction != null)) {
				firstInstruction = firstInstruction.getPrev();
			}
			InstructionHandle secondInstruction = firstInstruction.getPrev();
			Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());

			System.out.println("First number found and is:" + firstNumber);

			System.out.println("First number handled looking for second one");

			while (!(stackChangingOp(secondInstruction) || secondInstruction != null)) {
				System.out.println("SAM");
				secondInstruction = secondInstruction.getPrev();
			}
			Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

			System.out.println("second number found and is:" + secondNumber);

			// delete first instruction
			if (firstNumber == null || secondNumber == null) {
				return null;
			}

			safeInstructionDelete(instList, lastStackOp);
			if ((double) secondNumber == (double) firstNumber) {
				return 0;
			} else if ((double) secondNumber > (double) firstNumber) {
				return 1;
			} else {
				return -1;
			}
		} else if (lastStackOp.getInstruction() instanceof FCMPG) {

			System.out.println("So we found an CMPG instruction looking for first number");

			InstructionHandle firstInstruction = lastStackOp.getPrev();
			while (!(stackChangingOp(firstInstruction) || firstInstruction != null)) {
				firstInstruction = firstInstruction.getPrev();
			}
			InstructionHandle secondInstruction = firstInstruction.getPrev();
			Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());

			System.out.println("First number found and is:" + firstNumber);

			System.out.println("First number handled looking for second one");

			while (!(stackChangingOp(secondInstruction) || secondInstruction != null)) {
				System.out.println("SAM");
				secondInstruction = secondInstruction.getPrev();
			}
			Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

			System.out.println("second number found and is:" + secondNumber);

			// delete first instruction
			if (firstNumber == null || secondNumber == null) {
				return null;
			}

			safeInstructionDelete(instList, lastStackOp);
			if ((float) secondNumber == (float) firstNumber) {
				return 0;
			} else if ((float) secondNumber > (float) firstNumber) {
				return 1;
			} else {
				return -1;
			}
		} else if (lastStackOp.getInstruction() instanceof FCMPL) {

			System.out.println("So we found an CMPL instruction looking for first number");

			InstructionHandle firstInstruction = lastStackOp.getPrev();
			while (!(stackChangingOp(firstInstruction) || firstInstruction != null)) {
				firstInstruction = firstInstruction.getPrev();
			}
			InstructionHandle secondInstruction = firstInstruction.getPrev();
			Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());

			System.out.println("First number found and is:" + firstNumber);

			System.out.println("First number handled looking for second one");

			while (!(stackChangingOp(secondInstruction) || secondInstruction != null)) {
				System.out.println("SAM");
				secondInstruction = secondInstruction.getPrev();
			}
			Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

			System.out.println("second number found and is:" + secondNumber);

			// delete first instruction
			if (firstNumber == null || secondNumber == null) {
				return null;
			}

			safeInstructionDelete(instList, lastStackOp);
			if ((float) secondNumber == (float) firstNumber) {
				return 0;
			} else if ((float) secondNumber < (float) firstNumber) {
				return 1;
			} else {
				return -1;
			}
		} else if (lastStackOp.getInstruction() instanceof LDC) {
			// load the corresponding numba from the constant pool and return it :D
			LDC ldc = (LDC) lastStackOp.getInstruction();
			Number value = (Number) ldc.getValue(myCPGen);
			safeInstructionDelete(instList, lastStackOp);
			return value;
		}
		return null;
	}

	private Boolean stackChangingOp(InstructionHandle handle) {
		if (handle.getInstruction() instanceof ArithmeticInstruction && handle.getInstruction() instanceof BIPUSH && handle.getInstruction() instanceof SIPUSH && handle.getInstruction() instanceof LCONST && handle.getInstruction() instanceof DCONST && handle.getInstruction() instanceof FCONST && handle.getInstruction() instanceof ICONST && handle.getInstruction() instanceof DCMPG && handle.getInstruction() instanceof DCMPL && handle.getInstruction() instanceof FCMPG && handle.getInstruction() instanceof FCMPL && handle.getInstruction() instanceof LCMP && handle.getInstruction() instanceof LocalVariableInstruction && handle.getInstruction() instanceof StackInstruction) {
			return true;
		}
		return false;
	}

	private void replaceINCs(InstructionList instList) {
		for (InstructionHandle handle = instList.getStart(); handle != null; handle = handle.getNext()) {
			if (handle.getInstruction() instanceof IINC) {
				int incValue = ((IINC) handle.getInstruction()).getIncrement();
				int index = ((IINC) handle.getInstruction()).getIndex();
				instList.insert(handle, new BIPUSH((byte) incValue));
				InstructionHandle incBipush = handle.getPrev();
				instList.insert(handle, new ILOAD(index));
				instList.insert(handle, new IADD());
				instList.insert(handle, new ISTORE(index));
				try {
					instList.redirectBranches(handle, incBipush);
					instList.delete(handle);
				} catch (Exception e) {
					// do nothing
				}
				instList.setPositions();
			}
		}
	}

	private void safeInstructionDelete(InstructionList instList, InstructionHandle nodeToDelete) {
		instList.redirectBranches(nodeToDelete, nodeToDelete.getPrev());
		try {
			instList.delete(nodeToDelete);
		} catch (Exception e) {
			// do nothing
		}
	}

	public void optimize() {
		// load the original class into a class generator
		ClassGen cgen = new ClassGen(original);
		ConstantPoolGen cpgen = cgen.getConstantPool();

		// Do your optimization here
		Method[] methods = cgen.getMethods();
		for (Method m: methods) {
			System.out.println("Method: " + m);
			optimizeMethod(cgen, cpgen, m);

		}

		// Do your optimization here
		this.optimized = gen.getJavaClass();
	}

	public void write(String optimisedFilePath) {
		this.optimize();

		try {
			FileOutputStream out = new FileOutputStream(new File(optimisedFilePath));
			this.optimized.dump(out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

/*
  IF_ICMPEQ, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ICMPLT, IF_ICMPNE, IFEQ, IFGE, IFGT, IFLE, IFLT, IFNE

[java] Method: public int methodOneConstantVariableFolding()
     [java] CONSTANT_Integer[3](bytes = 54321)
     [java] 
     [java] 
     [java] 
     [java] 
     [java] Code after IINCS:
     [java]    0: bipush[16](2) 42
     [java]    2: istore_1[60](1)
     [java]    3: iload_1[27](1)
     [java]    4: sipush[17](3) 764
     [java]    7: iadd[96](1)
     [java]    8: iconst_3[6](1)
     [java]    9: imul[104](1)
     [java]   10: istore_2[61](1)
     [java]   11: iload_2[28](1)
     [java]   12: sipush[17](3) 1234
     [java]   15: iadd[96](1)
     [java]   16: iload_1[27](1)
     [java]   17: isub[100](1)
     [java]   18: ireturn[172](1)
     [java] 
     [java] THATS ALL
     [java] 
     [java] 
     [java] 
     [java] 
     [java] 
     [java] 
     [java] 
     [java]  optimising starts from here!!
     [java]    0: bipush[16](2) 42
     [java]    2: istore_1[60](1)
     [java] Found StoreInstruction!!!
     [java] 
     [java] Previous Stack operation was : 
     [java]    0: bipush[16](2) 42
     [java] STATUS : looking for iloads
     [java] looking at instruction:    3: iload_1[27](1)
     [java] the above instruction is a load and will be changed to bipush
     [java] This is how it looks now
     [java]    0: istore_1[60](1)
     [java]    1: bipush[16](2) 42
     [java]    3: sipush[17](3) 764
     [java]    6: iadd[96](1)
     [java]    7: iconst_3[6](1)
     [java]    8: imul[104](1)
     [java]    9: istore_2[61](1)
     [java]   10: iload_2[28](1)
     [java]   11: sipush[17](3) 1234
     [java]   14: iadd[96](1)
     [java]   15: iload_1[27](1)
     [java]   16: isub[100](1)
     [java]   17: ireturn[172](1)
     [java] looking at instruction:    3: sipush[17](3) 764
     [java] looking at instruction:    6: iadd[96](1)
     [java] looking at instruction:    7: iconst_3[6](1)
     [java] looking at instruction:    8: imul[104](1)
     [java] looking at instruction:    9: istore_2[61](1)
     [java] looking at instruction:   10: iload_2[28](1)
     [java] looking at instruction:   11: sipush[17](3) 1234
     [java] looking at instruction:   14: iadd[96](1)
     [java] looking at instruction:   15: iload_1[27](1)
     [java] the above instruction is a load and will be changed to bipush
     [java] This is how it looks now
     [java]    0: istore_1[60](1)
     [java]    1: bipush[16](2) 42
     [java]    3: sipush[17](3) 764
     [java]    6: iadd[96](1)
     [java]    7: iconst_3[6](1)
     [java]    8: imul[104](1)
     [java]    9: istore_2[61](1)
     [java]   10: iload_2[28](1)
     [java]   11: sipush[17](3) 1234
     [java]   14: iadd[96](1)
     [java]   15: bipush[16](2) 42
     [java]   17: isub[100](1)
     [java]   18: ireturn[172](1)
     [java] looking at instruction:   17: isub[100](1)
     [java] looking at instruction:   18: ireturn[172](1)
     [java]    0: istore_1[60](1)
     [java] found corresponding i store and giong out 
     [java]    0: bipush[16](2) 42
     [java]    2: sipush[17](3) 764
     [java]    5: iadd[96](1)
     [java]    6: iconst_3[6](1)
     [java]    7: imul[104](1)
     [java]    8: istore_2[61](1)
     [java] Found StoreInstruction!!!
     [java] 
     [java] Previous Stack operation was : 
     [java]    7: imul[104](1)
     [java] So we found an MUL instruction looking for first number
     [java] Previous Stack operation was : 
     [java]    6: iconst_3[6](1)
     [java] First number found and is:3
     [java] First number handled looking for second one
     [java] Previous Stack operation was : 
     [java]    5: iadd[96](1)
     [java] So we found an ADD instruction looking for first number
     [java] Previous Stack operation was : 
     [java]    2: sipush[17](3) 764
     [java] First number found and is:764
     [java] First number handled looking for second one
     [java] Previous Stack operation was : 
     [java]    0: bipush[16](2) 42
     [java] second number found and is:42
     [java] second number found and is:806
     [java] STATUS : looking for iloads
     [java] looking at instruction:    9: iload_2[28](1)
     [java] the above instruction is a load and will be changed to bipush
     [java] This is how it looks now
     [java]    0: istore_2[61](1)
     [java]    1: ldc[18](2) 18
     [java]    3: sipush[17](3) 1234
     [java]    6: iadd[96](1)
     [java]    7: bipush[16](2) 42
     [java]    9: isub[100](1)
     [java]   10: ireturn[172](1)
     [java] looking at instruction:    3: sipush[17](3) 1234
     [java] looking at instruction:    6: iadd[96](1)
     [java] looking at instruction:    7: bipush[16](2) 42
     [java] looking at instruction:    9: isub[100](1)
     [java] looking at instruction:   10: ireturn[172](1)
     [java]    0: istore_2[61](1)
     [java] found corresponding i store and giong out 
     [java]    0: ldc[18](2) 18
     [java]    2: sipush[17](3) 1234
     [java]    5: iadd[96](1)
     [java]    6: bipush[16](2) 42
     [java]    8: isub[100](1)
     [java]    9: ireturn[172](1)
     [java] SAMSAMSMASAMMSAMSAMASMSM
     [java]    0: ldc[18](2) 18
     [java]    2: sipush[17](3) 1234
     [java]    5: iadd[96](1)
     [java] Found ArithmeticInstruction!!!
     [java] 
     [java] Previous Stack operation was : 
     [java]    2: sipush[17](3) 1234
     [java] INVALID INSTRUCTION
     [java]    6: bipush[16](2) 42
     [java]    8: isub[100](1)
     [java] Found ArithmeticInstruction!!!
     [java] 
     [java] Previous Stack operation was : 
     [java]    6: bipush[16](2) 42
     [java] INVALID INSTRUCTION
     [java]    9: ireturn[172](1)
     [java] 
     [java] 
     [java] 
     [java] 
     [java] This is the whole code
     [java] 
     [java] 
     [java] 
     [java] 
     [java]    0: ldc[18](2) 18
     [java]    5: iadd[96](1)
     [java]    8: isub[100](1)
     [java]    9: ireturn[172](1)
     [java] 
     [java] 
     [java] 
     [java] done
     



0: ldc[18](2) 18
     [java]    2: ireturn[172](1)
  
  
  // Number something = ((Instructiontype)handle.getInstruction())).getValue();
  // Number something = (Number) ldc.getValue(myCPGen);
  */