package com.musaemotion.a2a.agent.server.mcp.manager;

import com.musaemotion.a2a.agent.server.mcp.model.McpBasics;
import io.modelcontextprotocol.spec.McpSchema;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/7/24  15:37
 * @description
 */
public abstract class ToolCapabilityLoader {

	protected  McpBasics mcpBasics;
	protected  McpSchema.ServerCapabilities serverCapabilities;
    protected ToolCapabilityLoader(McpBasics mcpBasics, McpSchema.ServerCapabilities serverCapabilities){
		this.mcpBasics = mcpBasics;
		this.serverCapabilities = serverCapabilities;
	}

	protected void loadTools(Supplier<List<McpSchema.Tool>> supplier){
		/*
		有的mcp估计没按照这个协议标准返回，不能用该方法判断，因为会返回空，应该用client直接返回
		if (serverCapabilities.tools() == null) {
			return;
		}
		if(serverCapabilities.tools().listChanged() ==null || !serverCapabilities.tools().listChanged()){
			return;
		}
		*/
		mcpBasics.setTools(supplier.get());
	}

	protected void loadResources(Supplier<List<McpSchema.Resource>> supplier){
		if (serverCapabilities.resources() == null ) {
			return;
		}
		if( serverCapabilities.resources() == null ||!serverCapabilities.resources().listChanged()){
			return;
		}
		mcpBasics.setResources(supplier.get());
	}

	protected void loadPrompts(Supplier<List<McpSchema.Prompt>> supplier){
		if (serverCapabilities.prompts() == null) {
			return;
		}
		if(serverCapabilities.prompts() == null && !serverCapabilities.prompts().listChanged()){
			return;
		}
		mcpBasics.setPrompts(supplier.get());
	}

	abstract void loadTools();

	abstract void loadResources();

	abstract void loadPrompts();
}
