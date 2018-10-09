/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.monitoring.gui;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class AdvancedMonitor extends JFrame {

    public AdvancedMonitor() {
        this("Advanced Monitoring Tool", 10, 10, 800, 600);
    }

    private AdvancedMonitor(String title, int x, int y, int width, int height) {
        setTitle(title);
        setBounds(x, y, width, height);
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();

        ThreadMonitorPanel threadMonitor = new ThreadMonitorPanel();
        threadMonitor.setVisible(true);

        ChunkMonitorPanel chunkMonitor = new ChunkMonitorPanel();
        chunkMonitor.setVisible(true);

        PerformanceMonitorPanel performanceMonitor = new PerformanceMonitorPanel();
        performanceMonitor.setVisible(true);

        tabs.add("Threads", threadMonitor);;
        tabs.add("Chunks", chunkMonitor);
        tabs.add("Performance", performanceMonitor);

        add(tabs, BorderLayout.CENTER);
    }
}
