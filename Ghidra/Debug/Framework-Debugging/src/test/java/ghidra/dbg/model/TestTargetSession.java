/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ghidra.dbg.model;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import ghidra.async.AsyncUtils;
import ghidra.dbg.agent.DefaultTargetModelRoot;
import ghidra.dbg.attributes.TargetObjectRef;
import ghidra.dbg.target.*;
import ghidra.dbg.target.TargetExecutionStateful.TargetExecutionState;

public class TestTargetSession extends DefaultTargetModelRoot
		implements TestTargetObject, TargetFocusScope<TestTargetSession>,
		TargetEventScope<TestTargetSession>, TargetLauncher<TestTargetSession> {
	public final TestTargetEnvironment environment;
	public final TestTargetProcessContainer processes;
	public final TestTargetInterpreter interpreter;
	public final TestMimickJavaLauncher mimickJavaLauncher;

	public TestTargetSession(TestDebuggerObjectModel model, String rootHint) {
		super(model, rootHint);
		environment = new TestTargetEnvironment(this);
		processes = new TestTargetProcessContainer(this);
		interpreter = new TestTargetInterpreter(this);
		mimickJavaLauncher = new TestMimickJavaLauncher(this);

		changeAttributes(List.of(), List.of(
			environment,
			processes,
			interpreter,
			mimickJavaLauncher),
			Map.of(), "Initialized");
	}

	public TestTargetProcess addProcess(int pid) {
		return processes.addProcess(pid);
	}

	@Override
	public TestDebuggerObjectModel getModel() {
		return (TestDebuggerObjectModel) super.getModel();
	}

	@Override
	public CompletableFuture<Void> requestFocus(TargetObjectRef obj) {
		return getModel().future(null).thenAccept(__ -> {
			changeAttributes(List.of(), List.of(), Map.of(
				FOCUS_ATTRIBUTE_NAME, obj //
			), "Focus requested");
			listeners.fire(TargetFocusScopeListener.class).focusChanged(this, obj);
		});
	}

	public void simulateStep(TestTargetThread eventThread) {
		eventThread.setState(TargetExecutionState.RUNNING);
		listeners.fire(TargetEventScopeListener.class)
				.event(this, eventThread, TargetEventType.STEP_COMPLETED,
					"Test thread completed a step", List.of());
		eventThread.setState(TargetExecutionState.STOPPED);
	}

	@Override
	public CompletableFuture<Void> launch(Map<String, ?> args) {
		// TODO: Record the request and allow tests to complete it?
		return AsyncUtils.NIL;
	}
}