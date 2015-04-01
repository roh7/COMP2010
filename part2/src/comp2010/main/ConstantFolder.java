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

		
		// Changes the iincs to 
		// bipush value
		// iload_i
		// iadd
		// istore_i
		for (InstructionHandle handle : instList.getInstructionHandles()) {
			if (handle.getInstruction() instanceof IINC) {
				int incValue = ((IINC)handle.getInstruction()).getIncrement();
				int index = ((IINC)handle.getInstruction()).getIndex();
				System.out.println("IINC FOUND" + handle);
				instList.insert(handle, new BIPUSH((byte)incValue));
				
				InstructionHandle incBipush = handle.getPrev();

				instList.insert(handle, new ILOAD(index));
				System.out.println(handle.getPrev());
				instList.insert(handle, new IADD());
				System.out.println(handle.getPrev());
				instList.insert(handle, new ISTORE(index));
				System.out.println(handle.getPrev());
				try {
					handleBranchInstructions(instList, handle, incBipush);
					instList.delete(handle);
				}
				catch(Exception e) {
					// do nothing
				}
			}
		}


		for (InstructionHandle handle : instList.getInstructionHandles()) {
			System.out.println(handle);
		}

		System.out.println("\n\n\n\n code starts from here!! \n\n\n\n");

		
		// // InstructionHandle is a wrapper for actual Instructions
		// for (InstructionHandle handle : instList.getInstructionHandles())
		// {
		// 	System.out.println(handle);
		// 	if (handle.getInstruction() instanceof StoreInstruction)
		// 	{
		// 		System.out.println("Found StoreInstruction!!!\n\n\n");
		// 		if (handle.getInstruction() instanceof ISTORE) {	
		// 			// istore
		// 			if (handle.getInstruction().getOpcode() == 54){
		// 				int value = (int) getLastStackPush(handle);
		// 				System.out.println("SAM HERE 1");
		// 				int istoreIndex = ((ISTORE)handle.getInstruction()).getIndex();
		// 				System.out.println("SAM HERE 2");
		// 				InstructionHandle handleNow = handle.getNext();
		// 				System.out.println("SAM HERE 3");
		// 				while (!(handleNow.getInstruction() instanceof ISTORE &&
		// 					    ((ISTORE)handle.getInstruction()).getIndex() == istoreIndex) &&
		// 					    handleNow != null) {
		// 					System.out.println("SAM HERE 3.1");
		// 					if (handleNow.getInstruction() instanceof ILOAD &&
		// 					    ((ILOAD)handleNow.getInstruction()).getIndex() == istoreIndex) {
		// 						System.out.println("SAM HERE 3.2");
		// 						instList.insert(handleNow, new BIPUSH((byte)value));
		// 						System.out.println("SAM HERE 3.3");
		// 						try {
		// 							System.out.println("SAM HERE 3.35");
		// 							System.out.println("HANDLENOW: " + handleNow.getPrev());
		// 							System.out.println("SAM HERE 3.4");

		// 							handleNow = handleNow.getNext();
		// 							InstructionHandle handleDelete = handleNow.getPrev();
		// 							instList.delete(handleDelete);
		// 							System.out.println("SAM HERE 3.5");
		// 							// System.out.println(handleNow + "CHECKING");
		// 						}
		// 						catch(Exception e) {
		// 							System.out.println("NPE FOUND");
		// 						}
		// 					}

		// 					else {
		// 						handleNow = handleNow.getNext();
		// 					}
		// 				}
		// 				//bipush value every time you see iload until you see istore again.
		// 			}


		// 			// istore_0
		// 			else if (handle.getInstruction().getOpcode() == 59){
		// 			}
		// 			// istore_1
		// 			else if (handle.getInstruction().getOpcode() == 60){
		// 			}
		// 			// istore_2
		// 			else if (handle.getInstruction().getOpcode() == 61){
		// 			}
		// 			// istore_3
		// 			else if (handle.getInstruction().getOpcode() == 62){
		// 			}
		// 			else
		// 			{
		// 				System.out.println("INVALID INSTRUCTION");
		// 			}

		// 		}
		// 		else if (handle.getInstruction() instanceof FSTORE) {

		// 		}
		// 		else if (handle.getInstruction() instanceof DSTORE) {	
		// 		}
		// 		else if (handle.getInstruction() instanceof LSTORE) {	
		// 		}
		// 		else if (handle.getInstruction() instanceof ASTORE) {	
		// 		}
		// 		else {
		// 			System.out.println("INVALID INSTRUCTION");
		// 		}
		// 		/*
		// 		InstructionHandle valueHolder = findLastStackPush(handle);
		// 		if (valueHolder.getInstruction() instanceof BIPUSH) {
		// 			System.out.println("ITS VALUE IS:" + ((BIPUSH)valueHolder.getInstruction()).getValue());
		// 			System.out.println(valueHolder);
		// 			System.out.println(handle);	
		// 		}	
		// 		else {
		// 			System.out.println("didnt find BIPUSH");
		// 			System.out.println(handle);
		// 		}*/		
		// 	}
		// }


		//System.console().readLine();

		// // setPositions(true) checks whether jump handles 
		// // are all within the current method
		// instList.setPositions(true);

		// // set max stack/local
		// methodGen.setMaxStack();
		// methodGen.setMaxLocals();

		// // generate the new method with replaced iconst
		// Method newMethod = methodGen.getMethod();
		// // replace the method in the original class
		// cgen.replaceMethod(method, newMethod);

	}

	private void handleBranchInstructions(InstructionList list, InstructionHandle handle, InstructionHandle newTarget) {
		InstructionHandle currentHandle = handle;
		while((currentHandle.getPrev() != null)) {
			currentHandle = currentHandle.getPrev();
			if(currentHandle.getInstruction() instanceof BranchInstruction) {
				if(((BranchInstruction)currentHandle.getInstruction()).getTarget().equals(handle)) {
					((BranchInstruction)currentHandle.getInstruction()).setTarget(newTarget);
				}
			}
		}
	}

	private Number getLastStackPush(InstructionHandle handle) {
		InstructionHandle lastStackOp = handle;
		do {
			System.out.println("Currently looking at:");
			System.out.println(lastStackOp);
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

		return 0;
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