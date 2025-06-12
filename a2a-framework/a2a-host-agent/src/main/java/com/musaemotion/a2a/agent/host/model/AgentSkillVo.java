package com.musaemotion.a2a.agent.host.model;

import com.google.common.collect.Lists;
import com.musaemotion.a2a.common.AgentSkill;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author labidc@hotmail.com
 * @version 1.0.0
 * @date 2025/6/10  20:31
 * @description 智能体能力显示
 */
@Data
public class AgentSkillVo {

	/**
	 * skillName 和 agentCard 的 name 不要重复，会影响 大模型推理的注意力，从而产生工具调用幻觉
	 */
	private String name;

	/**
	 * 描述
	 */
	private String description;

	/**
	 * tags
	 */
	private List<String> tags = new ArrayList<>();

	/**
	 * 示例
	 */
	private List<String> examples;

	/**
	 *
	 * @param agentSkill
	 * @return
	 */
	public static AgentSkillVo from(AgentSkill agentSkill){
		AgentSkillVo agentSkillVo = new AgentSkillVo();
		agentSkillVo.setName(agentSkill.getName());
		agentSkillVo.setDescription(agentSkill.getDescription());
		agentSkillVo.setTags(agentSkill.getTags());
		agentSkillVo.setExamples(agentSkill.getExamples());
		return agentSkillVo;
	}

	/**
	 *
	 * @param agentSkill
	 * @return
	 */
	public static List<AgentSkillVo> fromList(List<AgentSkill> agentSkill) {
		List<AgentSkillVo> agentSkillVoList = Lists.newArrayList();
		agentSkill.forEach(agentSkillVo -> agentSkillVoList.add(AgentSkillVo.from(agentSkillVo)));
		return agentSkillVoList;
	}
}
