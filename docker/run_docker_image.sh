#!/usr/bin/env bash

docker run --rm -it \
 --runtime=nvidia --gpus all \
 --entrypoint /bin/bash \
 --mount type=bind,src="/home/micha/Documents/LicenseFiles/gurobi_web_license.lic",target="/opt/gurobi/gurobi.lic" \
 --mount type=bind,src="$HOME/.moma",target="/root/.moma" \
 --mount type=bind,src="/home/micha/Documents/01_work/15_moma_notes/02_moma_development/feature/20230612-containerize-moma",target="/data" \
 --mount type=bind,src="$HOME",target="$HOME" \
moma:v0.9.3

#docker run --rm -it \
# --mount type=bind,src="/home/micha/Documents/LicenseFiles/gurobi_web_license.lic",target="/opt/gurobi/gurobi.lic" \
# --mount type=bind,src="$HOME/.moma",target="/root/.moma" \
# --mount type=bind,src="/home/micha/Documents/01_work/15_moma_notes/02_moma_development/feature/20230612-containerize-moma",target="/data" \
#moma:v0.9.3 \
# -p /data/mm.properties -i /data/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif -headless


#ln -s -f /build_dir/target/MotherMachine-v0.9.3.20230613-183815.617f156.jar /moma/MoMA_fiji.jar


#/moma/moma -headless -p /data/mm.properties -i /data/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif

#xvfb-run /moma/moma -headless -p /data/mm.properties -i /data/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif

# export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/local/cuda/lib64
# xvfb-run /moma/moma -headless -tmax 5 -p /data/mm.properties -i /data/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif



#xvfb-run java -Xmx32g -Djava.library.path="${GUROBI_LIB_PATH}":"${TF_JAVA_LIB_PATH}" -jar "${MOMA_JAR_PATH}"/"${MOMA_JAR_FILENAME}" "$@"



#java -Xmx32g -Djava.library.path="${GUROBI_LIB_PATH}":"${TF_JAVA_LIB_PATH}" -jar "${MOMA_JAR_PATH}"/"${MOMA_JAR_FILENAME}" -p /data/mm.properties -i /data/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif
#
#./moma -i /data/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif -p /data/mm.properties
#
#./moma -i /data/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif -p /data/mm.properties -analysis test_batch_run -trackonly -headless


# running in headless mode throws error about GurobiJni81 not being found
# Command: java -Xmx32g -Djava.library.path=/opt/gurobi/linux64/lib/:/../Tools/Fiji.app/lib/linux64 -jar MoMA_fiji.jar -i /data/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif -p /data/mm.properties -headless
#
# Error:
#>>> Starting MM in headless mode.
#Could not initialize Gurobi.
#You might not have installed Gurobi properly or you miss a valid license.
#Please visit 'www.gurobi.com' for further information.
#no GurobiJni81 in java.library.path: [/opt/gurobi/linux64/lib/, /../Tools/Fiji.app/lib/linux64]
#Java library path: /opt/gurobi/linux64/lib/:/../Tools/Fiji.app/lib/linux64
#>>>>> Java library path: /opt/gurobi/linux64/lib/:/../Tools/Fiji.app/lib/linux64

# running in non-headless mode throws error
# Command: java -Xmx32g -Djava.library.path=/opt/gurobi/linux64/lib/:/../Tools/Fiji.app/lib/linux64 -jar MoMA_fiji.jar -i /data/20211026_VNG1040_AB6min_2h_1_MMStack_Pos7_GL12.tif -p /data/mm.properties
#
# Error:
#Exception in thread "main" java.awt.HeadlessException:
#No X11 DISPLAY variable was set, but this program performed an operation which requires it.
#	at java.desktop/java.awt.GraphicsEnvironment.checkHeadless(GraphicsEnvironment.java:208)
#	at java.desktop/java.awt.Window.<init>(Window.java:548)
#	at java.desktop/java.awt.Frame.<init>(Frame.java:423)
#	at java.desktop/java.awt.Frame.<init>(Frame.java:388)
#	at java.desktop/javax.swing.SwingUtilities$SharedOwnerFrame.<init>(SwingUtilities.java:1919)
#	at java.desktop/javax.swing.SwingUtilities.getSharedOwnerFrame(SwingUtilities.java:1995)
#	at java.desktop/javax.swing.JOptionPane.getRootFrame(JOptionPane.java:1689)
#	at java.desktop/javax.swing.JOptionPane.showOptionDialog(JOptionPane.java:868)
#	at java.desktop/javax.swing.JOptionPane.showMessageDialog(JOptionPane.java:670)
#	at java.desktop/javax.swing.JOptionPane.showMessageDialog(JOptionPane.java:641)
#	at com.jug.intialization.SetupValidator.checkGurobiInstallation(SetupValidator.java:36)
#	at com.jug.MoMA.main(MoMA.java:74)