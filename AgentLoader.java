package com.devexperts.rmi.impl;

import com.sun.tools.attach.VirtualMachine;

import java.lang.management.ManagementFactory;

public class AgentLoader {

	public static void loadAgent(String jarFilePath, String options) throws Exception {
		String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
		String pid = nameOfRunningVM.substring(0, nameOfRunningVM.indexOf('@'));
		VirtualMachine vm = VirtualMachine.attach(pid);
		vm.loadAgent(jarFilePath, options);
		vm.detach();
	}
}
