package ac.entrypoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ac.entity.AsyncEntryPoint;
import ac.processor.AsyncTaskInterProceduralJudge;
import androlic.entrypoint.activity.AndroidCallBacks;
import androlic.entrypoint.activity.AndroidCallBacks.ActivityInfo;
import androlic.entrypoint.activity.AndroidEntryPointConstants;
import androlic.util.Log;
import androlic.util.MethodSynchronize;
import androlic.util.MethodUtil;
import soot.Body;
import soot.Local;
import soot.Modifier;
import soot.PrimType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.VoidType;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.AssignStmt;
import soot.jimple.EqExpr;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NewExpr;
import soot.jimple.NopStmt;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;
import soot.jimple.toolkits.scalar.NopEliminator;
import soot.util.MultiMap;

public class ActivityEntryMethod {

	private SootClass mActivityUnderAnalysis = null;

	private ActivityInfo mActivityInfo = null;

	private Body mBody = null;

	private SootMethod mainMethod = null;

	private List<Type> mArgList = new ArrayList<>();

	private Map<String, Integer> mArgTypeMap = new HashMap<>();

	private String METHOD_NAME = "dummyMain";

	private Map<SootClass, Local> localVarsForClasses = new HashMap<>();

	// private List<String> substituteClasses = null;

	private final Set<SootClass> failedClasses = new HashSet<>();

	private boolean warnOnConstructorLoop = true;

	private boolean shallowMode = false;

	private boolean allowSelfReferences = false;

	private LocalGenerator mLocalGenerator;

	private Local thisLocal = null;

	private Local intentLocal = null;
	private Local mOpaqueBooleans = null;

	private final Set<SootMethod> failedMethods = new HashSet<>();

//	private Value intCounter;
	private int conditionCounter;

	private SootField intentField = null;

	private Set<SootMethod> callbacks = new HashSet<>();

	private MultiMap<SootClass, SootMethod> callbackClasses;

	private Set<SootClass> invokedClasses = null;

	private AsyncEntryPoint newMethodEntry;

	public ActivityEntryMethod(AsyncEntryPoint entry, int index) {
		if (entry == null) {
			throw new IllegalArgumentException("AsyncEntryPoint should not be null");
		}
		if (entry.getNewEntryMethod() == null) {
			throw new IllegalArgumentException("AsyncEntryPoint.getNewEntryMethod() should not be null");
		}
		if (entry.getCallbackMethod() == null) {
			throw new IllegalArgumentException("AsyncEntryPoint.getCallbackMethod() should not be null");
		}

		METHOD_NAME = METHOD_NAME + index;

		this.newMethodEntry = entry;

		mActivityUnderAnalysis = entry.getCallbackMethod().getDeclaringClass();

		mActivityInfo = AndroidCallBacks.getSootClassesInvoked(mActivityUnderAnalysis, null, null);

		invokedClasses = mActivityInfo.mInvokedSootClasses;

		callbackClasses = AndroidCallBacks.getCallBackMultiMap(invokedClasses);

		mBody = Jimple.v().newBody();

		mLocalGenerator = new LocalGenerator(mBody);

		for (SootClass listenerClass : callbackClasses.keySet()) {

			callbacks.addAll(callbackClasses.get(listenerClass));

		}

		handleLayoutXml();

		for (SootMethod sootMethod : callbacks) {
			if (sootMethod.getParameterCount() > 0) {
				addArg(sootMethod);
			}
		}

		addArg("android.content.Intent");
		addArg("java.util.List");

		addArg(newMethodEntry.getNewEntryMethod());

		addActivityLifeCycleMethods();

		createAdditionalFields();
		createEmptyMainMethod();

		// Make sure that we have an opaque predicate
		conditionCounter = 0;
//		body.getUnits().add(Jimple.v().newAssignStmt(intCounter, IntConstant.v(conditionCounter)));

		createDummyMainInternal();

//		Log.println(mBody);

	}

	private void addArg(SootMethod sootMethod) {
		for (int i = 0; i < sootMethod.getParameterCount(); i++) {
			mArgList.add(sootMethod.getParameterType(i));
			String key = getTypeKey(sootMethod, sootMethod.getParameterType(i), i);
			mArgTypeMap.put(key, mArgList.size() - 1);
		}
	}

	private void addArg(String className) {
		mArgList.add(RefType.v(className));
		mArgTypeMap.put(className, mArgList.size() - 1);
	}

	private void handleLayoutXml() {
		if (AndroidCallBacks.sXMLCALLBACK_METHODS == null) {
			return;
		}
		MultiMap<String, String> xmlCALLBACK_METHODS = AndroidCallBacks.sXMLCALLBACK_METHODS;
		for (String str : xmlCALLBACK_METHODS.keySet()) {
			if (mActivityInfo.mInvokedLayoutXmls != null && mActivityInfo.mInvokedLayoutXmls.contains(str)) {
				for (String callback : xmlCALLBACK_METHODS.get(str)) {
					SootMethod sootMethod = MethodUtil.getMethod(mActivityUnderAnalysis, callback);
					if (sootMethod != null) {
						this.callbacks.add(sootMethod);
					}
				}
			}
		}
	}

	private void createEmptyMainMethod() {
		SootClass component = mActivityUnderAnalysis;
		// Generate a method name

		// Get the target method
		// int methodIndex = 0;

		String dummyClassName = mActivityUnderAnalysis.getName();
		SootClass mainClass = Scene.v().getSootClass(dummyClassName);

		// Remove the existing main method if necessary. Do not clear the
		// existing one, this would take much too long.
		mainMethod = mainClass.getMethodByNameUnsafe(METHOD_NAME);
		if (mainMethod != null) {
			mainClass.removeMethod(mainMethod);
			mainMethod = null;
		}

		final SootClass intentClass = Scene.v().getSootClassUnsafe("android.content.Intent");
		if (intentClass == null)
			throw new RuntimeException("Could not find Android intent class");

		// Create the method
		mArgList.add(RefType.v(intentClass));
		mainMethod = Scene.v().makeSootMethod(METHOD_NAME, mArgList, component.getType());

		// Create the body

		mBody.setMethod(mainMethod);
		mainMethod.setActiveBody(mBody);

		// Add the method to the class
		MethodSynchronize.addMethod(mainClass, mainMethod);
//		mainClass.addMethod(mainMethod);

		// First add class to scene, then make it an application class
		// as addClass contains a call to "setLibraryClass"
		mainClass.setApplicationClass();
		mainMethod.setModifiers(Modifier.PUBLIC | Modifier.STATIC);

		// Add the identity statements to the body. This must be done after the
		// method has been properly declared.
		((JimpleBody) mBody).insertIdentityStmts();

		// Get the parameter locals
		intentLocal = mBody.getParameterLocal(mArgTypeMap.get("android.content.Intent"));
		localVarsForClasses.put(intentClass, intentLocal);
		mOpaqueBooleans = mBody.getParameterLocal(mArgTypeMap.get("java.util.List"));
	}

	public SootMethod getMainMethod() {
//		Log.e("getMainMethod");
//		Log.e(mBody);
		return mainMethod;
	}

	private SootMethod createDummyMainInternal() {
		SootClass component = mActivityUnderAnalysis;
		// Before-class marker
		Stmt beforeComponentStmt = Jimple.v().newNopStmt();
		mBody.getUnits().add(beforeComponentStmt);

		Stmt endClassStmt = Jimple.v().newNopStmt();
		try {
			// We may skip the complete component
			createIfStmt(endClassStmt);

			// Create a new instance of the component
			thisLocal = generateClassConstructor(component);
			if (thisLocal == null) {
//				Log.e("Constructor cannot be generated for {}", component.getName());
			} else {
				localVarsForClasses.put(component, thisLocal);

				// Store the intent
				mBody.getUnits().add(Jimple.v()
						.newAssignStmt(Jimple.v().newInstanceFieldRef(thisLocal, intentField.makeRef()), intentLocal));

				// Create calls to the lifecycle methods
				generateComponentLifecycle();
			}
//			createIfStmt(beforeComponentStmt);

		} finally {
			mBody.getUnits().add(endClassStmt);
			if (thisLocal == null)
				mBody.getUnits().add(Jimple.v().newReturnStmt(NullConstant.v()));
			else
				mBody.getUnits().add(Jimple.v().newReturnStmt(thisLocal));
		}
		NopEliminator.v().transform(mBody);
		return mainMethod;
	}

	private void createAdditionalFields() {
		SootClass component = mActivityUnderAnalysis;
		// Create a name for a field for the intent with which the component is started
		String fieldName = "ipcIntent";
		int fieldIdx = 0;
		while (component.declaresFieldByName(fieldName))
			fieldName = "ipcIntent_" + fieldIdx++;

		// Create the field itself
		intentField = Scene.v().makeSootField(fieldName, RefType.v("android.content.Intent"), Modifier.PUBLIC);
		component.addField(intentField);
	}

	private void generateComponentLifecycle() {
		SootClass component = mActivityUnderAnalysis;
		Set<SootClass> currentClassSet = Collections.singleton(component);

		Set<SootClass> referenceClasses = new HashSet<>();
		referenceClasses.add(component);

		if (callbacks != null && !callbacks.isEmpty()) {
			Set<SootClass> callBackSet = new HashSet<SootClass>();
			for (SootMethod sootMethod : callbacks) {
				callBackSet.add(sootMethod.getDeclaringClass());
			}
			createClassInstances(callBackSet);
		}

		// 1. onCreate:
		{
			searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITY_ONCREATE, component, thisLocal);
			// for (SootClass callbackClass : this.activityLifecycleCallbacks.keySet()) {
			// searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITYLIFECYCLECALLBACK_ONACTIVITYCREATED,
			// callbackClass, localVarsForClasses.get(callbackClass), currentClassSet);
			// }
		}

		// 2. onStart:
		Stmt onStartStmt;
		{
			onStartStmt = searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITY_ONSTART, component, thisLocal);
			// for (SootClass callbackClass : this.activityLifecycleCallbacks.keySet()) {
			// Stmt s =
			// searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITYLIFECYCLECALLBACK_ONACTIVITYSTARTED,
			// callbackClass, localVarsForClasses.get(callbackClass), currentClassSet);
			// if (onStartStmt == null)
			// onStartStmt = s;
			// }

			// If we don't have an onStart method, we need to create a
			// placeholder so that we
			// have somewhere to jump
			if (onStartStmt == null)
				mBody.getUnits().add(onStartStmt = Jimple.v().newNopStmt());

		}
		// onRestoreInstanceState is optional, the system only calls it if a
		// state has previously been stored.
		{
			Stmt afterOnRestore = Jimple.v().newNopStmt();
			createIfStmt(afterOnRestore);
			searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITY_ONRESTOREINSTANCESTATE, component, thisLocal,
					currentClassSet);
			mBody.getUnits().add(afterOnRestore);
		}
		searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITY_ONPOSTCREATE, component, thisLocal);

		// 3. onResume:
		Stmt onResumeStmt = Jimple.v().newNopStmt();
		mBody.getUnits().add(onResumeStmt);
		{
			searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITY_ONRESUME, component, thisLocal);
			// for (SootClass callbackClass : this.activityLifecycleCallbacks.keySet()) {
			// searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITYLIFECYCLECALLBACK_ONACTIVITYRESUMED,
			// callbackClass, localVarsForClasses.get(callbackClass), currentClassSet);
			// }
		}
		searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITY_ONPOSTRESUME, component, thisLocal);

		// callbacks = AndroidCallBacks.getSootClassesInvoked(sootClass,
		// visitiedClasses, visitiedSootMethods)

		// Scan for other entryPoints of this class:
		if (this.callbacks != null && !this.callbacks.isEmpty()) {
			// NopStmt startWhileStmt = Jimple.v().newNopStmt();
			// NopStmt endWhileStmt = Jimple.v().newNopStmt();
			// body.getUnits().add(startWhileStmt);
			// createIfStmt(endWhileStmt);

			// Add the callbacks
			addCallbackMethods();
			//
			// body.getUnits().add(endWhileStmt);
			// createIfStmt(startWhileStmt);
		}

		// 4. onPause:
		searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITY_ONPAUSE, component, thisLocal);
		// for (SootClass callbackClass : this.activityLifecycleCallbacks.keySet()) {
		// searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITYLIFECYCLECALLBACK_ONACTIVITYPAUSED,
		// callbackClass,
		// localVarsForClasses.get(callbackClass), currentClassSet);
		// }
		searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITY_ONCREATEDESCRIPTION, component, thisLocal);
		searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITY_ONSAVEINSTANCESTATE, component, thisLocal);
		// for (SootClass callbackClass : this.activityLifecycleCallbacks.keySet()) {
		// searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITYLIFECYCLECALLBACK_ONACTIVITYSAVEINSTANCESTATE,
		// callbackClass, localVarsForClasses.get(callbackClass), currentClassSet);
		// }

		// goTo Stop, Resume or Create:
		// (to stop is fall-through, no need to add)
		createIfStmt(onResumeStmt);
		// createIfStmt(onCreateStmt); // no, the process gets killed in between

		// 5. onStop:
		@SuppressWarnings("unused")
		Stmt onStop = searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITY_ONSTOP, component, thisLocal);

		// goTo onDestroy, onRestart or onCreate:
		// (to restart is fall-through, no need to add)
		NopStmt stopToDestroyStmt = Jimple.v().newNopStmt();
		createIfStmt(stopToDestroyStmt);
		// createIfStmt(onCreateStmt); // no, the process gets killed in between

		// 6. onRestart:
		searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITY_ONRESTART, component, thisLocal);
		mBody.getUnits().add(Jimple.v().newGotoStmt(onStartStmt));
		// createIfStmt(onStartStmt); // jump to onStart(), fall through to
		// onDestroy()

		// 7. onDestroy
		mBody.getUnits().add(stopToDestroyStmt);
		searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITY_ONDESTROY, component, thisLocal);
		// for (SootClass callbackClass : this.activityLifecycleCallbacks.keySet()) {
		// searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITYLIFECYCLECALLBACK_ONACTIVITYDESTROYED,
		// callbackClass, localVarsForClasses.get(callbackClass), currentClassSet);
		// }
//		mBody.getUnits().add(Jimple.v().newReturnVoidStmt());
	}

	private void addCallbackMethods() {

		Stmt callBackStartStmt = null;
		for (SootMethod sootMethod : callbacks) {
			// NopStmt thenStmt = Jimple.v().newNopStmt();
			// body.getUnits().add(thenStmt);
			Local classLocal = this.localVarsForClasses.get(sootMethod.getDeclaringClass());
			Stmt stmt = buildMethodCall(sootMethod, classLocal, Collections.<SootClass>emptySet());
			if (callBackStartStmt == null) {
				callBackStartStmt = stmt;
			}
		}
		createIfStmt(callBackStartStmt);
	}

	private Stmt searchAndBuildMethod(String subsignature, SootClass currentClass, Local classLocal) {
		return searchAndBuildMethod(subsignature, currentClass, classLocal, Collections.<SootClass>emptySet());
	}

	private Stmt searchAndBuildMethod(String subsignature, SootClass currentClass, Local classLocal,
			Set<SootClass> parentClasses) {
		if (currentClass == null || classLocal == null) {
			return null;
		}

		SootMethod method = findMethod(currentClass, subsignature);
		if (method == null) {
			return null;
		}

		// If the method is in one of the predefined Android classes, it cannot
		// contain custom code, so we do not need to call it
		if (AndroidEntryPointConstants.isLifecycleClass(method.getDeclaringClass().getName())) {
			return null;
		}

		assert method.isStatic() || classLocal != null : "Class local was null for non-static method "
				+ method.getSignature();

		// write Method
		return buildMethodCall(method, classLocal, parentClasses);
	}

	/**
	 * Finds a method with the given signature in the given class or one of its
	 * super classes
	 * 
	 * @param currentClass The current class in which to start the search
	 * @param subsignature The subsignature of the method to find
	 * @return The method with the given signature if it has been found, otherwise
	 *         null
	 */
	private SootMethod findMethod(SootClass currentClass, String subsignature) {
		SootMethod m = currentClass.getMethodUnsafe(subsignature);
		if (m != null) {
			return m;
		}
		if (currentClass.hasSuperclass()) {
			return findMethod(currentClass.getSuperclass(), subsignature);
		}
		return null;
	}

	/**
	 * Builds a new invocation statement that invokes the given method
	 * 
	 * @param methodToCall  The method to call
	 * @param classLocal    The local containing an instance of the class on which
	 *                      to invoke the method
	 * @param parentClasses The classes for which we already have instances that
	 *                      shall be reused
	 * @return The newly created invocation statement
	 */
	private Stmt buildMethodCall(SootMethod methodToCall, Local classLocal, Set<SootClass> parentClasses) {

		// Log.e("buildMethodCall++methodToCall",null);
		// If we don't have a method, we cannot call it (sad but true)
		if (methodToCall == null) {
			return null;
		}

		if (classLocal == null && !methodToCall.isStatic()) {
//			Log.e("Cannot call method {}, because there is no local for base object: {}", methodToCall,
//					methodToCall.getDeclaringClass());
			failedMethods.add(methodToCall);
			// Log.e("buildMethodCall++classLocal == null && !methodToCall.isStatic()");
			return null;
		}
		
		if (!newMethodEntry.isNewMethodLifeCycleMethod() && methodToCall.equals(newMethodEntry.getCallbackMethod())) {
			classLocal = this.localVarsForClasses.get(methodToCall.getDeclaringClass());
			return buildMethodCall(newMethodEntry.getNewEntryMethod(), classLocal, Collections.<SootClass>emptySet());
		}
		if (!AsyncTaskInterProceduralJudge.v().isContainAsyncTaskOperation(methodToCall)) {
			return null;
		}

		final InvokeExpr invokeExpr;
		List<Value> args = new LinkedList<Value>();
		if (methodToCall.getParameterCount() > 0) {
			for (int i = 0; i < methodToCall.getParameterCount(); i++) {
				String key = getTypeKey(methodToCall, methodToCall.getParameterType(i), i);
				Set<SootClass> constructionStack = new HashSet<SootClass>();
				if (!allowSelfReferences)
					constructionStack.add(methodToCall.getDeclaringClass());
				if (mArgTypeMap.keySet().contains(key)) {
					args.add(mBody.getParameterLocal(mArgTypeMap.get(key)));
				} else {

					args.add(getValueForType(methodToCall.getParameterType(i)));
				}
			}

			if (methodToCall.isStatic())
				invokeExpr = Jimple.v().newStaticInvokeExpr(methodToCall.makeRef(), args);
			else {
				assert classLocal != null : "Class local method was null for non-static method call";
				if (methodToCall.isConstructor()) {
					invokeExpr = Jimple.v().newSpecialInvokeExpr(classLocal, methodToCall.makeRef(), args);
				} else {
					if (classLocal.getType() instanceof RefType
							&& ((RefType) classLocal.getType()).getSootClass().isInterface()) {
						invokeExpr = Jimple.v().newInterfaceInvokeExpr(classLocal, methodToCall.makeRef(), args);
					} else {
						invokeExpr = Jimple.v().newVirtualInvokeExpr(classLocal, methodToCall.makeRef(), args);
					}
				}
			}
		} else {
			if (methodToCall.isStatic()) {
				invokeExpr = Jimple.v().newStaticInvokeExpr(methodToCall.makeRef());
			} else {
				assert classLocal != null : "Class local method was null for non-static method call";
				if (methodToCall.isConstructor()) {
					invokeExpr = Jimple.v().newSpecialInvokeExpr(classLocal, methodToCall.makeRef());
				} else {
					if (classLocal.getType() instanceof RefType
							&& ((RefType) classLocal.getType()).getSootClass().isInterface()) {
						invokeExpr = Jimple.v().newInterfaceInvokeExpr(classLocal, methodToCall.makeRef(), args);
					} else {
						invokeExpr = Jimple.v().newVirtualInvokeExpr(classLocal, methodToCall.makeRef(), args);
					}
				}
			}
		}

		Stmt stmt;
		if (!(methodToCall.getReturnType() instanceof VoidType)) {
			Local returnLocal = mLocalGenerator.generateLocal(methodToCall.getReturnType());
			stmt = Jimple.v().newAssignStmt(returnLocal, invokeExpr);

		} else {
			stmt = Jimple.v().newInvokeStmt(invokeExpr);
		}
		// Log.e("buildMethodCall",stmt);
		mBody.getUnits().add(stmt);

		// Clean up. If we re-use parent objects, do not destroy those. We can
		// only clean up what we have created.
		for (Value val : args)
			if (val instanceof Local && val.getType() instanceof RefType) {
				if (!parentClasses.contains(((RefType) val.getType()).getSootClass())) {
//					body.getUnits().add(Jimple.v().newAssignStmt(val, NullConstant.v()));
					localVarsForClasses.remove(((RefType) val.getType()).getSootClass());
				}
			}

		return stmt;
	}

	private void addActivityLifeCycleMethods() {
		SootClass activitySelf = Scene.v().getSootClass(AndroidEntryPointConstants.ACTIVITYCLASS);
		if (activitySelf == null) {
			activitySelf = Scene.v().getSootClass(AndroidEntryPointConstants.APPCOMPATACTIVITYCLASS_V4);
		}
		if (activitySelf == null) {
			activitySelf = Scene.v().getSootClass(AndroidEntryPointConstants.APPCOMPATACTIVITYCLASS_V7);
		}
		addActivityLifeCycleMethod(AndroidEntryPointConstants.ACTIVITY_ONCREATE, activitySelf);
		addActivityLifeCycleMethod(AndroidEntryPointConstants.ACTIVITY_ONSTART, activitySelf);
		addActivityLifeCycleMethod(AndroidEntryPointConstants.ACTIVITY_ONRESTART, activitySelf);
		addActivityLifeCycleMethod(AndroidEntryPointConstants.ACTIVITY_ONRESUME, activitySelf);
		addActivityLifeCycleMethod(AndroidEntryPointConstants.ACTIVITY_ONPAUSE, activitySelf);
		addActivityLifeCycleMethod(AndroidEntryPointConstants.ACTIVITY_ONSAVEINSTANCESTATE, activitySelf);
		addActivityLifeCycleMethod(AndroidEntryPointConstants.ACTIVITY_ONRESTOREINSTANCESTATE, activitySelf);
		addActivityLifeCycleMethod(AndroidEntryPointConstants.ACTIVITY_ONSTOP, activitySelf);
		addActivityLifeCycleMethod(AndroidEntryPointConstants.ACTIVITY_ONPOSTRESUME, activitySelf);
		addActivityLifeCycleMethod(AndroidEntryPointConstants.ACTIVITY_ONPOSTCREATE, activitySelf);
		addActivityLifeCycleMethod(AndroidEntryPointConstants.ACTIVITY_ONCREATEDESCRIPTION, activitySelf);
		addActivityLifeCycleMethod(AndroidEntryPointConstants.ACTIVITY_ONDESTROY, activitySelf);
	}

	private void addActivityLifeCycleMethod(String methodName, SootClass activitySelf) {
		addLifeCycleMethod(methodName, mActivityUnderAnalysis, activitySelf);
	}

	private void addLifeCycleMethod(String methodName, SootClass myClass, SootClass libClass) {
		SootMethod sootMethod = findMethod(myClass, methodName);
		SootMethod superMethod = null;
		try {
			superMethod = libClass.getMethodUnsafe(methodName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.println("we cannot find lifecycle method ", methodName, " in class ", libClass.getName(),
					". Maybe the method wasnot existed in current Android level");
			// e.printStackTrace();
		}
		if (superMethod == null) {
			new Exception("superMethod == null").printStackTrace();
			return;
		}

		if (sootMethod == null || sootMethod.getSignature().equals(superMethod.getSignature())) {
			sootMethod = Scene.v().makeSootMethod(superMethod.getName(), superMethod.getParameterTypes(),
					superMethod.getReturnType(), superMethod.getModifiers());
			MethodSynchronize.addMethod(myClass, sootMethod);
//			myClass.addMethod(sootMethod);
			JimpleBody jimpleBody = Jimple.v().newBody(sootMethod);
			jimpleBody.insertIdentityStmts();
			sootMethod.setActiveBody(jimpleBody);

			final InvokeExpr invokeExpr = Jimple.v().newSpecialInvokeExpr(jimpleBody.getThisLocal(),
					superMethod.makeRef(), jimpleBody.getParameterLocals());
			if (superMethod.getReturnType() instanceof VoidType) {
				Stmt stmt = Jimple.v().newInvokeStmt(invokeExpr);
				jimpleBody.getUnits().add(stmt);
				jimpleBody.getUnits().add(Jimple.v().newReturnVoidStmt());
			} else {
				Local returnLocal = mLocalGenerator.generateLocal(superMethod.getReturnType());
				Stmt stmt = Jimple.v().newAssignStmt(returnLocal, invokeExpr);
				jimpleBody.getUnits().add(stmt);
				jimpleBody.getUnits().add(Jimple.v().newReturnStmt(returnLocal));
			}
		}
		if (sootMethod.equals(newMethodEntry.getCallbackMethod())) {
			return;
		}
		if (!AsyncTaskInterProceduralJudge.v().isContainAsyncTaskOperation(sootMethod)) {
			return;
		}
		addArg(sootMethod);
	}

	private void createIfStmt(Unit target) {
		if (target == null) {
			return;
		}
		final Jimple jimple = Jimple.v();
		Local r = mLocalGenerator.generateLocal(RefType.v("java.lang.Object"));
		SootMethod listGet = Scene.v().getSootClass("java.util.List").getMethod("java.lang.Object get(int)");
		Local b = mLocalGenerator.generateLocal(RefType.v("java.lang.Boolean"));
		final InvokeExpr invokeExpr = Jimple.v().newInterfaceInvokeExpr(mOpaqueBooleans, listGet.makeRef(),
				IntConstant.v(conditionCounter++));
		mBody.getUnits().add(Jimple.v().newAssignStmt(r, invokeExpr));
		mBody.getUnits().add(Jimple.v().newAssignStmt(b, Jimple.v().newCastExpr(r, RefType.v("java.lang.Boolean"))));
		EqExpr cond = jimple.newEqExpr(b, IntConstant.v(0));
		IfStmt ifStmt = jimple.newIfStmt(cond, target);
		mBody.getUnits().add(ifStmt);
	}

	/**
	 * Creates instance of the given classes
	 * 
	 * @param classes The classes of which to create instances
	 */
	private void createClassInstances(Collection<SootClass> classes) {
		for (SootClass callbackClass : classes) {
//			NopStmt thenStmt = Jimple.v().newNopStmt();
//			createIfStmt(thenStmt);
			Local l = localVarsForClasses.get(callbackClass);
			if (l == null) {
				l = generateClassConstructor(callbackClass);
				if (l != null)
					localVarsForClasses.put(callbackClass, l);
			}
//			mBody.getUnits().add(thenStmt);
		}
	}

	/**
	 * Generates code which creates a new instance of the given class.
	 * 
	 * @param createdClass      The class of which to create an instance
	 * @param constructionStack The stack of classes currently under construction.
	 *                          This is used to detect constructor loops. If a
	 *                          constructor requires a parameter of a type that is
	 *                          already on the stack, this value is substituted by
	 *                          null.
	 * @param parentClasses     If a constructor call requires an object of a type
	 *                          which is compatible with one of the types in this
	 *                          list, the already-created object is used instead of
	 *                          creating a new one.
	 * @param tempLocals        The set that receives all generated temporary locals
	 *                          that were necessary for calling the constructor of
	 *                          the requested class
	 * @return The local containing the new object instance if the operation
	 *         completed successfully, otherwise null.
	 */
	private Local generateClassConstructor(SootClass createdClass, Set<SootClass> constructionStack,
			Set<SootClass> parentClasses, Set<Local> tempLocals) {
		if (createdClass == null || this.failedClasses.contains(createdClass))
			return null;

		// If we already have a class local of that type, we re-use it
		Local existingLocal = localVarsForClasses.get(createdClass);
		if (existingLocal != null)
			return existingLocal;

		// We cannot create instances of phantom classes as we do not have any
		// constructor information for them
		if (createdClass.isPhantom() || createdClass.isPhantomClass()) {
//			Log.e("Cannot generate constructor for phantom class {}", createdClass.getName());
			failedClasses.add(createdClass);
			return null;
		}

		// if sootClass is simpleClass:
		if (isSimpleType(createdClass.toString())) {
			Local varLocal = getValueForType(createdClass.getType());
			AssignStmt assignStmt = Jimple.v().newAssignStmt(varLocal,
					mBody.getParameterLocal(mBody.getParameterLocals().size() - 1));
			mBody.getUnits().add(assignStmt);
			return varLocal;
		}

		boolean isInnerClass = createdClass.getName().contains("$");
		SootClass outerClass = isInnerClass
				? Scene.v().getSootClassUnsafe(
						createdClass.getName().substring(0, createdClass.getName().lastIndexOf("$")))
				: null;

		// Make sure that we don't run into loops
		if (!constructionStack.add(createdClass)) {
			if (warnOnConstructorLoop) {
//				Log.e("Ran into a constructor generation loop for class " + createdClass
//						+ ", substituting with null...");
			}
			Local tempLocal = mLocalGenerator.generateLocal(RefType.v(createdClass));
//			AssignStmt assignStmt = Jimple.v().newAssignStmt(tempLocal, NullConstant.v());
//			Log.e(assignStmt);
//			body.getUnits().add(assignStmt);
			return tempLocal;
		}
		if (createdClass.isInterface() || createdClass.isAbstract()) {
			return generateSubstitutedClassConstructor(createdClass, mBody, constructionStack, parentClasses);
		} else {
			// Find a constructor we can invoke. We do this first as we don't
			// want
			// to change anything in our method body if we cannot create a class
			// instance anyway.
			List<SootMethod> constructors = new ArrayList<>();
			for (SootMethod currentMethod : MethodSynchronize.getSootMethods(createdClass)) {
				if (currentMethod.isPrivate() || currentMethod.isProtected() || !currentMethod.isConstructor())
					continue;
				constructors.add(currentMethod);
			}

			// The fewer parameters a constructor has, the better for us
			Collections.sort(constructors, new Comparator<SootMethod>() {

				@Override
				public int compare(SootMethod o1, SootMethod o2) {
					if (o1.getParameterCount() == o2.getParameterCount()) {
						int o1Prims = 0, o2Prims = 0;
						for (int i = 0; i < o1.getParameterCount(); i++)
							if (o1.getParameterType(i) instanceof PrimType)
								o1Prims++;
						for (int i = 0; i < o2.getParameterCount(); i++)
							if (o2.getParameterType(i) instanceof PrimType)
								o2Prims++;
						return o1Prims - o2Prims;
					}
					return o1.getParameterCount() - o2.getParameterCount();
				}

			});

			if (!constructors.isEmpty()) {
				SootMethod currentMethod = constructors.remove(0);
				List<Value> params = new LinkedList<Value>();
				for (Type type : currentMethod.getParameterTypes()) {
					// We need to reset the construction stack. Just because we
					// already created a class instance for parameter 1, there is no reason for
					// not being able to create the same class instance again for parameter 2.

					// We need to check whether we have a reference to the
					// outer class. In this case, we do not generate a new
					// instance, but use the one we already have.
					SootClass typeClass = type instanceof RefType ? ((RefType) type).getSootClass() : null;
					if (typeClass != null && isInnerClass && typeClass == outerClass
							&& this.localVarsForClasses.containsKey(outerClass))
						params.add(this.localVarsForClasses.get(outerClass));
					else if (shallowMode) {
						if (isSimpleType(type.toString()))
							params.add(getValueForType(type));
						else
							params.add(NullConstant.v());
					} else {
						Value val = getValueForType(type);
						AssignStmt assignStmt = Jimple.v().newAssignStmt(val,
								mBody.getParameterLocal(mBody.getParameterLocals().size() - 1));
						mBody.getUnits().add(assignStmt);
						params.add(val);
					}
				}

				// Build the "new" expression
				NewExpr newExpr = Jimple.v().newNewExpr(RefType.v(createdClass));
				Local tempLocal = mLocalGenerator.generateLocal(RefType.v(createdClass));
				AssignStmt assignStmt = Jimple.v().newAssignStmt(tempLocal, newExpr);
				mBody.getUnits().add(assignStmt);

				// Create the constructor invocation
				InvokeExpr vInvokeExpr;
				if (params.isEmpty() || params.contains(null))
					vInvokeExpr = Jimple.v().newSpecialInvokeExpr(tempLocal, currentMethod.makeRef());
				else
					vInvokeExpr = Jimple.v().newSpecialInvokeExpr(tempLocal, currentMethod.makeRef(), params);

				// We don't need return values
				mBody.getUnits().add(Jimple.v().newInvokeStmt(vInvokeExpr));
				if (tempLocals != null)
					tempLocals.add(tempLocal);
				return tempLocal;
			}

//			Log.e("Could not find a suitable constructor for class {}", createdClass.getName());
			this.failedClasses.add(createdClass);
			return null;
		}
	}

	/**
	 * Generates code which creates a new instance of the given class.
	 * 
	 * @param createdClass The class of which to create an instance
	 * @param body         The body to which to add the new statements ("new"
	 *                     statement, constructor call, etc.)
	 * @return The local containing the new object instance if the operation
	 *         completed successfully, otherwise null.
	 */
	private Local generateClassConstructor(SootClass createdClass) {
		return this.generateClassConstructor(createdClass, new HashSet<SootClass>(), Collections.<SootClass>emptySet(),
				null);
	}

	private static boolean isSimpleType(String t) {
		if (t.equals("java.lang.String") || t.equals("void") || t.equals("char") || t.equals("byte")
				|| t.equals("short") || t.equals("int") || t.equals("float") || t.equals("long") || t.equals("double")
				|| t.equals("boolean")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Generates a call to a constructor for a an interface or an abstract class
	 * that is substituted with an actual implementation
	 * 
	 * @param createdClass      The class for which to create a constructor call
	 * @param body              The body of the dummy main method to which to add
	 *                          the call statement
	 * @param constructionStack The stack for making sure that we do not run into
	 *                          loops
	 * @param parentClasses     If a constructor call requires an object of a type
	 *                          which is compatible with one of the types in this
	 *                          list, the already-created object is used instead of
	 *                          creating a new one.
	 * @return The local containing the new object instance if the operation
	 *         completed successfully, otherwise null.
	 */
	private Local generateSubstitutedClassConstructor(SootClass createdClass, Body body,
			Set<SootClass> constructionStack, Set<SootClass> parentClasses) {
		// This feature must be enabled explicitly
		// if (!substituteCallParams) {
		// Log.e("Cannot create valid constructor for {}, because it is {} and cannot
		// substitute with subclass",
		// createdClass,
		// (createdClass.isInterface() ? "an interface" : (createdClass.isAbstract() ?
		// "abstract" : "")));
		// this.failedClasses.add(createdClass);
		// return null;
		// }

		// Find a matching implementor of the interface
		// List<SootClass> classes;
		// if (createdClass.isInterface())
		// classes = Scene.v().getActiveHierarchy().getImplementersOf(createdClass);
		// else
		// classes = Scene.v().getActiveHierarchy().getSubclassesOf(createdClass);

		// Generate an instance of the substitution class. If we fail,
		// try the next substitution. If we don't find any possible
		// substitution, we're in trouble
		// for (SootClass sClass : classes)
		// if (substituteClasses.contains(sClass.toString())) {
		// Local cons = generateClassConstructor(sClass, body, constructionStack,
		// parentClasses, null);
		// if (cons == null)
		// continue;
		// return cons;
		// }
//		Log.e("Cannot create valid constructor for {}, because it is {} and cannot substitute with subclass",
//				createdClass,
//				(createdClass.isInterface() ? "an interface" : (createdClass.isAbstract() ? "abstract" : "")));
		this.failedClasses.add(createdClass);
		return null;
	}

	private int parameterIndex = 0;

	/**
	 * Creates a value of the given type to be used as a substitution in method
	 * invocations or fields
	 * 
	 * @param body              The body in which to create the value
	 * @param gen               The local generator
	 * @param tp                The type for which to get a value
	 * @param constructionStack The set of classes we're currently constructing.
	 *                          Attempts to create a parameter of one of these
	 *                          classes will trigger the constructor loop check and
	 *                          the respective parameter will be substituted by
	 *                          null.
	 * @param parentClasses     If the given type is compatible with one of the
	 *                          types in this list, the already-created object is
	 *                          used instead of creating a new one.
	 * @param generatedLocals   The set that receives all (temporary) locals created
	 *                          to provide a value of the requested type
	 * @return The generated value, or null if no value could be generated
	 */
	private Local getValueForType(Type tp) {
		return mBody.getParameterLocal(parameterIndex++ % mBody.getParameterLocals().size());
	}

	private String getTypeKey(SootMethod sootMethod, Type type, int i) {
		return sootMethod.getSignature() + type + i;
	}

//	public static SootMethod create() {
//		return new ActivityEntryMethod(newNewMethodEntry(), 22222).getMainMethod();
//	}
//
//	public static NewMethodEntry newNewMethodEntry() {
//		NewMethodEntry newMethodEntry = new NewMethodEntry();
//		newMethodEntry.lifeCycleMethod = Scene.v().getSootClass("fr.neamar.kiss.MainActivity")
//				.getMethodByName("onResume");
//		newMethodEntry.newSootMethod = Scene.v().getSootClass("fr.neamar.kiss.MainActivity")
//				.getMethod("void displayKissBar(boolean,boolean)");
//		return newMethodEntry;
//	}

	public static SootMethod create(AsyncEntryPoint newMethodEntry2, int i) {
		return new ActivityEntryMethod(newMethodEntry2, i).getMainMethod();
	}

}
