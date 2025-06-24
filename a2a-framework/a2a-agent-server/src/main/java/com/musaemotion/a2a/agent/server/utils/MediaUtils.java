/*
 * Copyright (c) 2025 MusaeMotion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.musaemotion.a2a.agent.server.utils;

import com.musaemotion.a2a.common.base.Common;
import jakarta.xml.bind.DatatypeConverter;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.MimeType;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author：contact@musaemotion.com
 * @package：com.musaemotion.a2a.agent.server.utils
 * @project：A2A
 * @date：2025/5/13 11:38
 * @description：请完善描述
 */
public class MediaUtils {

	/**
	 * filePart 到  media 转换
	 * @param fileParts filePart 列表
	 * @return
	 */
	public static List<Media> filePartToMedia(List<Common.FilePart> fileParts) {
        List<Media> medias = new ArrayList<>();
        fileParts.forEach(filePart->{
            Common.FileContent fileContent = filePart.getFile();
            if (StringUtils.hasText(fileContent.getUri())) {
				medias.add(Media.builder()
						.mimeType(new MimeType(filePart.getType()))
						.data(URI.create(fileContent.getUri()))
						.build());
            }
            if (StringUtils.hasText(fileContent.getBytes())) {
                int commaIndex = fileContent.getBytes().indexOf(',');
                byte[] decodedBytes;
                if (commaIndex == -1) {
                    decodedBytes = DatatypeConverter.parseBase64Binary(fileContent.getBytes());
                } else {
                    decodedBytes = DatatypeConverter.parseBase64Binary(fileContent.getBytes().substring(commaIndex + 1));
                }
                medias.add(new Media(MimeType.valueOf(fileContent.getMimeType()), new ByteArrayResource(decodedBytes)));
            }
        });
        return medias;
    }
}
