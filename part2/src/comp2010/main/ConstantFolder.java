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
import org.apache.bcel.generic.BranchInstruction;

import org.apache.bcel.generic.LocalVariableInstruction;
import org.apache.bcel.generic.StackInstruction;

public class ConstantFolder
{
	ClassParser parser = null;
	ClassGen gen = null;

	JavaClass original = null;
	JavaClass optimized = null;

	// Keeps track of what instructions we have
	// or will be visiting. true -> needs to be visited
	// false-> shouldn't or have already been visited
	ArrayList<Boolean> instructions = new ArrayList<Boolean>();

	public ConstantFolder(String classFilePath)
	{
		try{
			this.parser = new ClassParser(classFilePath);
			this.original = this.parser.parse();
			this.gen = new ClassGen(this.original);
		} catch(IOException e){
			e.printStackTrace();
		}
	}

	private void optimizeMethod (ClassGen cgen, ConstantPoolGen cpgen, Method method)
	{
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
		for (int i = 0; i < constants.length; i++)
		{
			if (constants[i] instanceof ConstantInteger)
				System.out.println(constants[i]);
				
		}

		InstructionHandle valueHolder;
		
		// Changes the iincs to :
		// bipush value, iload_i, iadd, istore_i.
		for (InstructionHandle handle = instList.getStart(); handle != null; handle = handle.getNext()) {
			if (handle.getInstruction() instanceof IINC) {
				int incValue = ((IINC)handle.getInstruction()).getIncrement();
				int index = ((IINC)handle.getInstruction()).getIndex();
				instList.insert(handle, new BIPUSH((byte)incValue));
				InstructionHandle incBipush = handle.getPrev();
				instList.insert(handle, new ILOAD(index));
				instList.insert(handle, new IADD());
				instList.insert(handle, new ISTORE(index));
				try {
					instList.redirectBranches(handle, incBipush);
					instList.delete(handle);
				}
				catch(Exception e) {
					// do nothing
				}
				instList.setPositions();
			}
		}
		

		System.out.println("\n\n\n\nCode after IINCS:");
		for (InstructionHandle handle : instList.getInstructionHandles()) {
			System.out.println(handle);
		}
		System.out.println("\nTHATS ALL\n\n\n");
		
		System.out.println("\n\n\n\n optimising starts from here!!");
		// InstructionHandle is a wrapper for actual Instructions
		for (InstructionHandle handle = instList.getStart(); handle != null; handle = handle.getNext())
		{
			System.out.println(handle);
			if (handle.getInstruction() instanceof StoreInstruction)
			{
				System.out.println("Found StoreInstruction!!!\n");
				Number lastStackValue = getLastStackPush(instList, handle);
				handleStoreInstructions(instList, handle, lastStackValue);
			}
		}

		System.out.println("\n\n\n\nThis is the whole code\n\n\n\n");
		for (InstructionHandle handle : instList.getInstructionHandles()) {
			System.out.println(handle);
		}
		System.out.println("\n\n\ndone\n\n");

		//System.console().readLine();

		// // set max stack/local
		// methodGen.setMaxStack();
		// methodGen.setMaxLocals();

		// // generate the new method with replaced iconst
		// Method newMethod = methodGen.getMethod();
		// // replace the method in the original class
		// cgen.replaceMethod(method, newMethod);

	}

	private void handleStoreInstructions(InstructionList instList, InstructionHandle handle, Number lastStackValue) {

		if (handle.getInstruction() instanceof ISTORE && lastStackValue != null) {	
			int value = (int) lastStackValue;
			int istoreIndex = ((ISTORE)handle.getInstruction()).getIndex();
			InstructionHandle handleNow = handle.getNext();
			
			System.out.println("STATUS : looking for iloads");

			while (handleNow != null &&
					!(handleNow.getInstruction() instanceof ISTORE &&
				    ((ISTORE)handle.getInstruction()).getIndex() == istoreIndex)) {
				System.out.print("looking at instruction: ");
				System.out.println(handleNow);
				if (handleNow.getInstruction() instanceof ILOAD &&
				    ((ILOAD)handleNow.getInstruction()).getIndex() == istoreIndex) {
					System.out.println("the above instruction is a load");
					instList.insert(handleNow, new BIPUSH((byte)value));
					try {
						handleNow = handleNow.getNext();
						InstructionHandle handleDelete = handleNow.getPrev();
						instList.redirectBranches(handleDelete, handleDelete.getPrev());
						instList.delete(handleDelete);
						instList.setPositions();
					}
					catch(Exception e) {
						//do nothing
					}
				}
				else {
					handleNow = handleNow.getNext();
				}
			}
			System.out.println("STATUS : Found Store Instruction and continuing with this instruciton");
			System.out.println(handle);
		}
		else if (handle.getInstruction() instanceof FSTORE) {

		}
		else if (handle.getInstruction() instanceof DSTORE) {	
		}
		else if (handle.getInstruction() instanceof LSTORE) {	
		}
		else if (handle.getInstruction() instanceof ASTORE) {	
		}
		else {
			System.out.println("INVALID INSTRUCTION");
		}
	}

	private Number getLastStackPush(InstructionList instList, InstructionHandle handle) {
		InstructionHandle lastStackOp = handle;
		do {
			lastStackOp = lastStackOp.getPrev();
		} while(!(stackChangingOp(lastStackOp) || lastStackOp != null));


		System.out.println("Previous Stack operation was : ");
		System.out.println(lastStackOp);

		if (lastStackOp.getInstruction() instanceof BIPUSH) {
			return ((BIPUSH)lastStackOp.getInstruction()).getValue();
		}
		else if (lastStackOp.getInstruction() instanceof SIPUSH) {
			return ((SIPUSH)lastStackOp.getInstruction()).getValue();
		}
		else if (lastStackOp.getInstruction() instanceof ICONST) {
			return ((ICONST)lastStackOp.getInstruction()).getValue();
		}
		else if (lastStackOp.getInstruction() instanceof DCONST) {
			return ((DCONST)lastStackOp.getInstruction()).getValue();
		}
		else if (lastStackOp.getInstruction() instanceof FCONST) {
			return ((FCONST)lastStackOp.getInstruction()).getValue();
		}
		else if (lastStackOp.getInstruction() instanceof LCONST) {
			return ((LCONST)lastStackOp.getInstruction()).getValue();
		}
		else if (lastStackOp.getInstruction() instanceof IADD) {
			
			System.out.println("So we found an ADD instruction looking for first number");
			InstructionHandle firstInstruction = lastStackOp;
			do {
				firstInstruction = firstInstruction.getPrev();
				System.out.println(firstInstruction);
			} while(!(stackChangingOp(firstInstruction) || firstInstruction != null));
			Number firstNumber = getLastStackPush(instList, firstInstruction.getNext());
			InstructionHandle secondInstruction = firstInstruction;

			System.out.println("First number found and is:" + firstNumber);
			
			

			System.out.println("First Number handled looking for second one");

			do {
				secondInstruction = secondInstruction.getPrev();
			} while(!(stackChangingOp(secondInstruction) || secondInstruction != null));
			Number secondNumber = getLastStackPush(instList, secondInstruction.getNext());

			System.out.println("second number found and is:" + secondNumber);


			// delete first instruction
			if (firstNumber != null) {
				instList.redirectBranches(firstInstruction, firstInstruction.getPrev());
				try {
					instList.delete(firstInstruction);
				}
				catch (Exception e) {
					// do nothing
				}
			}
			else {
				return null;
			}

			// delete second instruction
			if (secondNumber != null) {
				instList.redirectBranches(secondInstruction, secondInstruction.getPrev());
				try {
					instList.delete(secondInstruction);
				}
				catch (Exception e) {
					// do nothing
				}
			}
			else {
				return null;
			}

			return ((int)firstNumber + (int)secondNumber);
		}

		return null;
	}

	private Boolean stackChangingOp(InstructionHandle handle) {
		if (handle.getInstruction() instanceof ArithmeticInstruction &&
			     handle.getInstruction() instanceof BIPUSH &&
			     handle.getInstruction() instanceof SIPUSH &&
			     handle.getInstruction() instanceof LCONST &&
			     handle.getInstruction() instanceof DCONST &&
			     handle.getInstruction() instanceof FCONST &&
			     handle.getInstruction() instanceof ICONST &&
			     handle.getInstruction() instanceof DCMPG &&
			     handle.getInstruction() instanceof DCMPL &&
			     handle.getInstruction() instanceof FCMPG &&
			     handle.getInstruction() instanceof FCMPL &&
			     handle.getInstruction() instanceof LCMP &&
			     handle.getInstruction() instanceof LocalVariableInstruction &&
			     handle.getInstruction() instanceof StackInstruction) {
			return true;
		}
		return false;
	}

	public void optimize()
	{
		// load the original class into a class generator
		ClassGen cgen = new ClassGen(original);
		ConstantPoolGen cpgen = cgen.getConstantPool();

		// Do your optimization here
		Method[] methods = cgen.getMethods();
		for (Method m : methods)
		{
			System.out.println("Method: " + m);
			optimizeMethod(cgen, cpgen, m);

		}

		// Do your optimization here
		this.optimized = gen.getJavaClass();
	}
	
	public void write(String optimisedFilePath)
	{
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