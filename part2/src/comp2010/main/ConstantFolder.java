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
				System.out.println("IINC found " + handle.getInstruction());
				int incValue = ((IINC)handle.getInstruction()).getIncrement();
				int index = ((IINC)handle.getInstruction()).getIndex();
				instList.insert(handle, new BIPUSH((byte)incValue));
				instList.insert(handle, new ILOAD(index));
				instList.insert(handle, new IADD());
				instList.insert(handle, new ISTORE(index));
				try {
					instList.delete(handle);
				}
				catch(Exception e) {
					// do nothing
				}
			}
		}

		// InstructionHandle is a wrapper for actual Instructions
		for (InstructionHandle handle : instList.getInstructionHandles())
		{
			if (handle.getInstruction() instanceof StoreInstruction)
			{
				if (handle.getInstruction() instanceof ISTORE) {
					// istore
					if (handle.getInstruction().getOpcode() == 54) {
						Number value = getLastStackPush(handle);
						// System.out.println("ISTORE FOUND");
						//bipush value every time you see iload until you see istore again.
					}
					// istore_0
					if (handle.getInstruction().getOpcode() == 59) {
						// System.out.println("ISTORE_0 FOUND");
					}
					// istore_1
					if (handle.getInstruction().getOpcode() == 60) {
						// System.out.println("ISTORE_1 FOUND");
					}
					// istore_2
					if (handle.getInstruction().getOpcode() == 61) {
						// System.out.println("ISTORE_2 FOUND");
					}
					// istore_3
					if (handle.getInstruction().getOpcode() == 62) {
						// System.out.println("ISTORE_3 FOUND");
					}

				}
				else if (handle.getInstruction() instanceof FSTORE) {
					// System.out.println("FSTORE FOUND");
				}
				else if (handle.getInstruction() instanceof DSTORE) {
					// System.out.println("DSTORE FOUND");
				}
				else if (handle.getInstruction() instanceof LSTORE) {
					// System.out.println("LSTORE FOUND");
				}
				else if (handle.getInstruction() instanceof ASTORE) {
					// System.out.println("ASTORE FOUND");
				}
				else {
					System.out.println("INVALID DATA FORMAT");
				}
				/*
				InstructionHandle valueHolder = findLastStackPush(handle);
				if (valueHolder.getInstruction() instanceof BIPUSH) {
					System.out.println("ITS VALUE IS:" + ((BIPUSH)valueHolder.getInstruction()).getValue());
					System.out.println(valueHolder);
					System.out.println(handle);	
				}	
				else {
					System.out.println("didnt find BIPUSH");
					System.out.println(handle);
				}*/		
			}
		}

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

	private Number getLastStackPush(InstructionHandle handle) {
		do {
			handle = handle.getPrev();
			// System.out.println("THE HANDLE IS " + handle);
		} while(handle.getPrev() != null && !stackChangingOp(handle));

		if (handle.getInstruction() instanceof BIPUSH) {
			return ((BIPUSH)handle.getInstruction()).getValue();
		}
		else if (handle.getInstruction() instanceof SIPUSH) {
			return ((SIPUSH)handle.getInstruction()).getValue();
		}
		else if (handle.getInstruction() instanceof ICONST) {
			return ((ICONST)handle.getInstruction()).getValue();
		}
		else if (handle.getInstruction() instanceof DCONST) {
			return ((DCONST)handle.getInstruction()).getValue();
		}
		else if (handle.getInstruction() instanceof FCONST) {
			return ((FCONST)handle.getInstruction()).getValue();
		}
		else if (handle.getInstruction() instanceof LCONST) {
			return ((LCONST)handle.getInstruction()).getValue();
		}
		// if (handle.getInstruction() instanceof FCONST) {
		// 	return handle;
		// }
		// if (handle.getInstruction() instanceof LCONST) {
		// 	return handle;
		// }
		// if (handle.getInstruction() instanceof DCONST) {
		// 	return handle;
		// }
		// if (handle.getInstruction() instanceof ICONST) {
		// 	return handle;
		// }
		
		return 0;
	}

	private boolean stackChangingOp(InstructionHandle handle) {
		if (handle == null) {
			return false;
		}
		else if (handle.getInstruction() instanceof ArithmeticInstruction &&
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