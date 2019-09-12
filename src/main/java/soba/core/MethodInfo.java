package soba.core;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import soba.core.method.CallSite;
import soba.core.method.ControlDependence;
import soba.core.method.DataDependence;
import soba.core.method.FieldAccess;
import soba.core.method.OpcodeString;
import soba.core.method.asm.DataFlowAnalyzer;
import soba.core.method.asm.DataFlowInterpreter;
import soba.core.signature.MethodSignatureReader;
import soba.util.ObjectIdMap;
import soba.util.graph.DirectedGraph;

/**
 * This class represents a java method.
 */
public class MethodInfo {
	
	private ClassInfo ownerClass;
	private MethodNode method;

	private String returnType;
	private String[] paramTypes;
	private int[] paramIndex;
	private int paramCount;
	private boolean[] paramGeneric;
	
	private int[] lines;
	private int maxLine;
	private int minLine;
	
	private DataFlowAnalyzer analyzer;
	private DataDependence dataDependence;

	/**
	 * Creates a new <code>MethodInfo</code> instance.
	 * @param owner is a <code>ClassInfo</code> object which declares this method.
	 * @param method
	 */
	public MethodInfo(ClassInfo owner, MethodNode method) {
		this.ownerClass = owner;
		this.method = method;
	}
	
	/**
	 * @return the package name who has the method.
	 */
	public String getPackageName() {
		return ownerClass.getPackageName();
	}

	/**
	 * @return the class name who has the method.
	 */
	public String getClassName() {
		return ownerClass.getClassName();
	}

	/**
	 * @return a method name
	 */
	public String getMethodName() {
		return method.name;
	}
	
	/**
	 * @return the descriptor of the method.
	 */
	public String getDescriptor() {
		return method.desc;
	}
	
	/**
	 * @return a descriptor including generics information.
	 */
	public String getGenericsSignature() {
		return method.signature;
	}

	/**
	 * @return true if this method has a method body.
	 */
	public boolean hasMethodBody() {
		return (method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0;
	}

	/**
	 * @return true if this is a library method.
	 */
	public boolean isLibrary() {
		return ownerClass.isLibrary();
	}
	
	/**
	 * @return true if this method is declared as a static method.
	 */
	public boolean isStatic() {
		return (method.access & Opcodes.ACC_STATIC) != 0;
	}

	/**
	 * @return true if this method is automatically generated by the compiler. 
	 */
	public boolean isSynthetic() {
		return (method.access & Opcodes.ACC_SYNTHETIC) != 0;
	}

	/**
	 * @return true if this method is declared as a public method.
	 */
	public boolean isPublic() {
		return (method.access & Opcodes.ACC_PUBLIC) != 0;
	}

	/**
	 * @return true if this method is declared as a protected method.
	 */
	public boolean isProtected() {
		return (method.access & Opcodes.ACC_PROTECTED) != 0;
	}

	/**
	 * @return true if this method is declared as a private method.
	 */
	public boolean isPrivate() {
		return (method.access & Opcodes.ACC_PRIVATE) != 0;
	}

	/**
	 * @return true if this method can be accessed by the same package only.
	 */
	public boolean isPackagePrivate() {
		return (method.access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED)) == 0;
	}
	
	/**
	 * @return true if the method may be overridden by a subclass.
	 * In other words, the method is a non-final, non-private instance method. 
	 */
	public boolean isOverridable() {
		return (method.access & (Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | Opcodes.ACC_STATIC)) == 0;
	}

	/**
	 * @return the number of bytecode instructions in this method.
	 */
	public int getInstructionCount() {
		return method.instructions.size();
	}
	
	/**
	 * @param instructionIndex
	 * @return AbstractInsnNode of instructionIndex
	 */
	public AbstractInsnNode getAbstractInsnNode(int instructionIndex) {
		return method.instructions.get(instructionIndex);
	}
	
	/**
	 * @return the return value type.
	 * The method may return a generic type name such as "T". 
	 */
	public String getReturnType() {
		extractParametersIfNecessary();
		return returnType;
	}
	
	/**
	 * @return the number of parameters of this method.
	 */
	public int getParamCount() {
		extractParametersIfNecessary();
		return paramCount;
	}
	
	/**
	 * @return the index value of a receiver object.
	 */
	public int getReceiverObjectParamIndex() {
		assert !isStatic();
		return 0; // "this" is always the first argument even if a method is invoked for an inner/anonymous class.
	}
	
	/**
	 * This method translates "N-th" parameter to an index value for the local variable table.
	 * This method is required because a double-word (long, double) variable 
	 * occupies two words in a local variable table. 
	 * @param paramIndex specifies the position of a parameter.
	 * E.g. 0, 1, 2, ... indicate the first, the second, the third, ... parameters. 
	 * @return index value to accesss local variable table.
	 */
	public int getVariableTableIndexOfParamAt(int index) {
		return this.paramIndex[index];
	}
	
	/**
	 * This method is reverse of getVariableTableIndexOfParamAt.
	 * @return a position of the parameter corresponding to an index value for the local variable table.
	 */
	public int getParameterOrderingNumber(int localVarialbleIndex) {
		for (int p = 0; p < this.paramCount; p++) {
			if (paramIndex[p] == localVarialbleIndex) {
				return p;
			}
		}
		throw new IllegalArgumentException("getParameterOrderingNumber:" + localVarialbleIndex + " is not Parameter");
	}
	
	/**
	 * @param localVarialbleIndex specifies an index value for the local variable table.
	 * @return true if a local variable specified by the index value is a parameter of this method.
	 */
	public boolean isParameterOrderingNumber(int localVarialbleIndex) {
		for (int p = 0; p < this.paramCount; p++) {
			if (paramIndex[p] == localVarialbleIndex) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param index specifies the position of a parameter.
	 * @return a type name for the parameter.  
	 * A class name is fully qualified.
	 * The name may be a generic type parameter such as "T".
	 * An inner class name is concatinated by ".". 
	 * For example, "A.B" is returned for a type "A<T>.B".  
	 */
	public String getParamType(int index) {
		extractParametersIfNecessary();
		if (index >= paramTypes.length) return null;
		return paramTypes[index];
	}
	
	/**
	 * @param index specifies the position of a parameter.
	 * @return a formal parameter name. 
	 */
	public String getParamName(int index) {
		if (method.localVariables == null) return null;
		if (index >= method.localVariables.size()) return null;
		int paramIndex = getVariableTableIndexOfParamAt(index);
		for (int i=0; i<method.localVariables.size(); ++i) {
			LocalVariableNode var = (LocalVariableNode)method.localVariables.get(i);
			if (var.index == paramIndex && var.start == method.instructions.getFirst()) {
				return var.name;
			}
		}
		return null;
	}
	
	private void extractParametersIfNecessary() {
		if (paramTypes != null) return;
		
		MethodSignatureReader reader = new MethodSignatureReader(method.desc);
		int thisParam = isStatic() ? 0: 1;   
		
		// read type names
		this.returnType = reader.getReturnType();
		this.paramCount = reader.getParamCount() + thisParam;
		String[] params = new String[this.paramCount];
		if (!isStatic()) {
			if (getClassName() != null) {
				params[0] = getClassName();
			} else {
				params[0] = "(Owner-Class)";
			}
		}
		for (int i=0; i<reader.getParamCount(); ++i) {
			params[i+thisParam] = reader.getParamType(i);
		}
		
		// read generics flag
		paramGeneric = new boolean[this.paramCount];
		for (int i=0; i<reader.getParamCount(); ++i) {
			paramGeneric[i+thisParam] = reader.isGenericType(i);
		}
	
		// compute index for local variable table
		this.paramIndex = new int[params.length];
		int index = 0;
		for (int i=0; i<params.length; ++i) {
			paramIndex[i] = index;
			if (params[i].equals("double") || params[i].equals("long")) {
				// Double and Long are double word values.
				index += 2;
			} else {
				index += 1;
			}
		}
		
		// finished
		this.paramTypes = params;
	}

	/**
	 * @return a method node.
	 */
	public MethodNode getMethodNode() {
		return method;
	}
	
	private void computeMinMaxLine() {
		if (lines == null) {
			TIntHashSet array = new TIntHashSet(method.instructions.size());
			for (int i=0; i<method.instructions.size(); ++i) {
				if (method.instructions.get(i).getType() == AbstractInsnNode.LINE) {
					LineNumberNode node = (LineNumberNode)method.instructions.get(i);
					array.add(node.line);
				}
			}
			if (array.isEmpty()) {
				lines = new int[0];
				maxLine = 0;
				minLine = 0;
			} else {
				lines = array.toArray();
				Arrays.sort(lines);
				maxLine = lines[lines.length - 1];
				minLine = lines[0];
			}
		}
	}
	
	
	/**
	 * @return the maximum line including an instruction of the method.
	 * 0 indicates the method has no line number information.
	 */
	public int getMaxLine() {
		computeMinMaxLine();
		return maxLine;
	}
	
	/**
	 * @return the minimum line including an instruction of the method.
	 * 0 indicates the method has no line number information.
	 */
	public int getMinLine() {
		computeMinMaxLine();
		return minLine;
	}
	
	/**
	 * @return an array which is filled with the numbers from minimum line to maximum line.
	 */
	public int[] getLineNumbers() {
		computeMinMaxLine();
		return lines;
	}

	/**
	 * @param instructionIndex
	 * @return the line number including a specified instruction. 
	 */
	public int getLine(int instructionIndex) {
		for (int i=instructionIndex; i>=0; --i) {
			if (method.instructions.get(i).getType() == AbstractInsnNode.LINE) {
				return ((LineNumberNode)method.instructions.get(i)).line;
			}
		}
		return 0;
	}
	
	/**
	 * @param line
	 * @return an array of instruction index values that consist of a specified line.
	 */
	public int[] getInstructions(int line) {
		TIntArrayList lineInstructions = new TIntArrayList();
		boolean inside = false;
		for (int i=0; i<method.instructions.size(); ++i) {
			if (method.instructions.get(i).getType() == AbstractInsnNode.LINE) {
				inside = ((LineNumberNode)method.instructions.get(i)).line == line;
			}
			if (inside) lineInstructions.add(i);
		}
		return lineInstructions.toArray();
	}

	/**
	 * Returns a list of invocations in the method body.
	 * @return a list of <code>CallSite</code>.
	 */
	public List<CallSite> getCallSites() {
		List<CallSite> callsites = new ArrayList<CallSite>(method.instructions.size());
		for (int i=0; i<method.instructions.size(); ++i) {
			CallSite c = getCallSite(i);
			if (c != null) callsites.add(c);
		}
		return callsites;
	}
	
	/**
	 * Returns an invocation in a method call instruction. 
	 * If the instruction is not a method call, this method returns null.
	 * @param instructionIndex
	 * @return a <code>CallSite</code> object for an instruction.
	 */
	public CallSite getCallSite(final int instructionIndex) {
		if (method.instructions.get(instructionIndex).getType() == AbstractInsnNode.METHOD_INSN) {
			MethodInsnNode m = (MethodInsnNode)method.instructions.get(instructionIndex);
			return new CallSite(this, instructionIndex, m.owner, m.name, m.desc, getInvokeType(m));
		} else {
			return null;
		}
	}
	
	private CallSite.Kind getInvokeType(MethodInsnNode m) {
		CallSite.Kind k = CallSite.Kind.VIRTUAL;
		if (m.getOpcode() == Opcodes.INVOKESTATIC) k = CallSite.Kind.STATIC;
		else if (m.getOpcode() == Opcodes.INVOKESPECIAL) k = CallSite.Kind.SPECIAL;
		return k;
	}
	
	/**
	 * Returns a list of field accesses in the method body.
	 * @return a list of <code>FieldAccess</code>.
	 */
	public List<FieldAccess> getFieldAccesses() {
		List<FieldAccess> fields = new ArrayList<FieldAccess>(32);
		for (int i=0; i<method.instructions.size(); ++i) {
			if (method.instructions.get(i).getType() == AbstractInsnNode.FIELD_INSN) {
				FieldAccess fieldAccess = getFieldAccess(i);
				if (fieldAccess != null) { 
					fields.add(fieldAccess);
				}
			}
		}
		return fields;
	}
	
	/**
	 * Returns a field access in an instruction.
	 * If the instruction is not a field access, this method returns null.
	 * @param instructionIndex
	 * @return a <FieldAccess> object.
	 */
	public FieldAccess getFieldAccess(final int instructionIndex) {
		assert method.instructions.get(instructionIndex).getType() == AbstractInsnNode.FIELD_INSN;
		
		final FieldInsnNode f = (FieldInsnNode)method.instructions.get(instructionIndex);
		switch (f.getOpcode()) {
		case Opcodes.PUTFIELD:
			return FieldAccess.createPutField(f.owner, f.name, f.desc, false);
		case Opcodes.PUTSTATIC:
			return FieldAccess.createPutField(f.owner, f.name, f.desc, true);
		case Opcodes.GETFIELD:
			return FieldAccess.createGetField(f.owner, f.name, f.desc, false);
		case Opcodes.GETSTATIC:
			return FieldAccess.createGetField(f.owner, f.name, f.desc, true);
		default:
			assert false: "Unknown Field Operation Found.";
		}
		return null;
	}
	
	/**
	 * @return an array of index values for return instructions.
	 */
	public int[] getReturnInstructions() {
		TIntSet returns = new TIntHashSet();
		for (int i=0; i<method.instructions.size(); i++) {
			AbstractInsnNode ain = method.instructions.get(i);
			if (OpcodeString.isReturnOperation(ain)) {
				returns.add(i);
			}
		}
		return returns.toArray();
	}

	/**
	 * @return a <code>DataDependence</code> object.
	 * The object has information about data dependencies.
	 */
	public DataDependence getDataDependence() {
		computeFlow();
		return dataDependence;
	}
	
	/**
	 * @return a control dependence graph.
	 */
	public DirectedGraph getControlDependence() {
		return ControlDependence.getDependence(getInstructionCount(), getControlFlow());
	}
	
	/**
	 * @return a control-flow graph.
	 */
	public DirectedGraph getControlFlow() {
		computeFlow();
		return new DirectedGraph(getInstructionCount(), analyzer.getNormalControlFlow());
	}
	
	/**
	 * @return a conservative control-flow graph.
	 * The graph assumes that every instruction in a try block may throw an exception.
	 */
	public DirectedGraph getConservativeControlFlow() {
		computeFlow();
		return new DirectedGraph(getInstructionCount(), analyzer.getConservativeControlFlow());
	}
	
	private void computeFlow() {
		if (analyzer == null) {
			ObjectIdMap<AbstractInsnNode> instructions = new ObjectIdMap<AbstractInsnNode>(method.instructions.size());
			for (int i=0; i<method.instructions.size(); ++i) {
				instructions.add(method.instructions.get(i));
			}
			instructions.freeze();
			DataFlowInterpreter interpreter = new DataFlowInterpreter(instructions);
			analyzer = new DataFlowAnalyzer(interpreter);
			try {
				analyzer.analyze(method.name, method);
				dataDependence = new DataDependence(instructions, analyzer);
			} catch (AnalyzerException e) {
				System.err.println(e.getMessage());
			}
		}
	}
	
	/**
	 * @return a unique string that specifies the method declaration. 
	 */
	public String getMethodKey() {
		return getClassName() + "#" + getMethodName() + "#" + getDescriptor();
	}

	/**
	 * @param instructionIndex
	 * @return a string representation of the specified instruction.
	 */
	public String getInstructionString(final int instructionIndex) {
		return OpcodeString.getInstructionString(method, instructionIndex);
	}
	
	/**
	 * @return a shorter string representation of the method signature.
	 */
	public String toString() {
		return getMethodName() + getDescriptor();
	}

	/**
	 * @return a longer string representation of the method signature.
	 */
	public String toLongString() {
		StringBuilder name = new StringBuilder();
		if (getClassName() != null) {
			name.append(getClassName());
			name.append(".");
		}
		name.append(getMethodName());
		name.append("(");
		for (int i=0; i<getParamCount(); ++i) {
			if (i>0) name.append(", ");
			name.append(getParamType(i));
			String paramName = getParamName(i);
			if (paramName != null) {
				name.append(":");
				name.append(paramName);
			}
		}
		name.append(")");
		name.append(": ");
		name.append(getReturnType());
		return name.toString();
	}
}
