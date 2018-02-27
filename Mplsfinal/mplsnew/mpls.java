package net.floodlightcontroller.mplsnew;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.action.OFActionPushMpls;
import org.projectfloodlight.openflow.protocol.action.OFActionPushVlan;
import org.projectfloodlight.openflow.protocol.action.OFActionSetField;
import org.projectfloodlight.openflow.protocol.action.OFActionSetNwDst;
import org.projectfloodlight.openflow.protocol.action.OFActions;
import org.projectfloodlight.openflow.protocol.instruction.OFInstruction;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionApplyActions;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructions;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.oxm.OFOxms;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IPv4AddressWithMask;
import org.projectfloodlight.openflow.types.IPv6Address;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.OFVlanVidMatch;
import org.projectfloodlight.openflow.types.TransportPort;
import org.projectfloodlight.openflow.types.U32;
import org.projectfloodlight.openflow.types.U8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.PortChangeType;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.staticentry.IStaticEntryPusherService;

public class mpls implements IFloodlightModule, IOFSwitchListener {
	
	private static IStaticEntryPusherService sfps;
	private static IOFSwitchService switchService;
    private static Logger log;
    
	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IStaticEntryPusherService.class);
		l.add(IOFSwitchService.class);
         return l;
		// TODO Auto-generated method stub
		//return null;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		// TODO Auto-generated method stub
		{
			sfps = context.getServiceImpl(IStaticEntryPusherService.class);
			switchService = context.getServiceImpl(IOFSwitchService.class);
			switchService.addOFSwitchListener(this);
			log = LoggerFactory.getLogger(mpls.class);
			if (sfps == null) {
				log.error("Static Flow Pusher Service not found!");
			}
		}

	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		// TODO Auto-generated method stub
		switchService = context.getServiceImpl(IOFSwitchService.class);
		switchService.addOFSwitchListener(this);
		



	}

	@Override
	public void switchAdded(DatapathId switchId) {
OFFactory factory = switchService.getSwitch(switchId).getOFFactory();
		
		/*OFFlowAdd.Builder fab = factory.buildFlowAdd();
		fab.setMatch(factory.buildMatch().setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setMasked(MatchField.IPV4_SRC, IPv4Address.of("10.0.123.1"), IPv4Address.of("255.255.0.255"))
				.build());
		fab.setBufferId(OFBufferId.NO_BUFFER);
		if (switchId.equals(DatapathId.of(1)))
		switchService.getSwitch(switchId).write(fab.build());*/
		
		OFFactory f =factory;
        Match.Builder mb =f.buildMatch();
        
        //mb.setExact(MatchField.IN_PORT, OFPort.of(2));
        
        	    mb.setExact(MatchField.IN_PORT, OFPort.of(2));
        	   // mb.setExact(MatchField.ETH_TYPE, EthType.IPv4);
        	    //mb.setExact(MatchField.IPV4_SRC, IPv4Address.of("10.0.0.2"));
        	    //mb.setExact(MatchField.IPV4_DST, IPv4Address.of("10.0.0.1"));
        	   // mb.setExact(MatchField.IP_PROTO, IpProtocol.TCP);
        	    //mb.setExact(MatchField.TCP_DST, TransportPort.of(80));
        	    
        	    
        	    
        
       
       
        
        Match m=mb.build();
        ArrayList<OFAction> actionList = new ArrayList<OFAction>();
        OFActions actions = f.actions();
       OFActionPushMpls mpls = actions.pushMpls(EthType.of(0x8847));
        actionList.add(mpls);


        OFOxms oxms =f.oxms();
        OFActionSetField mplsL= actions.buildSetField()
        	    .setField(
        	        oxms.buildMplsLabel()
        	        .setValue(U32.of(18))
        	        .build()
        	    )
        	    .build();
        actionList.add(mplsL);
        //OFOxms oxms =f.oxms();
        OFActionOutput output = actions.buildOutput()
        	    .setMaxLen(0xFFffFFff)
        	    .setPort(OFPort.of(1))
        	    .build();
        	actionList.add(output);
        
       
        
        OFInstructions inst=f.instructions(); 
        OFInstructionApplyActions apply=inst.buildApplyActions().setActions(actionList).build();
        ArrayList<OFInstruction> instList= new ArrayList<OFInstruction>();
        instList.add(apply);
        OFFlowMod.Builder fmb = factory.buildFlowAdd();
        OFFlowMod msg = fmb.setPriority(32769)
        .setMatch(m)
        .setInstructions(instList)
        .setOutPort(OFPort.of(1))
        .build();
        
        
        
        
        Match.Builder mb1 =f.buildMatch();
        
        //mb.setExact(MatchField.IN_PORT, OFPort.of(2));
        
        	    mb1.setExact(MatchField.IN_PORT, OFPort.of(1));
        	   // mb.setExact(MatchField.ETH_TYPE, EthType.IPv4);
        	    //mb.setExact(MatchField.IPV4_SRC, IPv4Address.of("10.0.0.2"));
        	    //mb.setExact(MatchField.IPV4_DST, IPv4Address.of("10.0.0.1"));
        	   // mb.setExact(MatchField.IP_PROTO, IpProtocol.TCP);
        	    //mb.setExact(MatchField.TCP_DST, TransportPort.of(80));
        	    
        	    
        	    
        
       
       
        
        Match m1=mb1.build();
        ArrayList<OFAction> actionList1 = new ArrayList<OFAction>();
        OFActions actions1 = f.actions();
        OFActionPushMpls mpls1 = actions1.pushMpls(EthType.of(0x8847));
        actionList1.add(mpls1);


       
        OFActionSetField mplsL1= actions1.buildSetField()
        	    .setField(
        	        oxms.buildMplsLabel()
        	        .setValue(U32.of(20))
        	        .build()
        	    )
        	    .build();
        actionList1.add(mplsL1);
        //OFOxms oxms =f.oxms();
        OFActionOutput output1 = actions1.buildOutput()
        	    .setMaxLen(0xFFffFFff)
        	    .setPort(OFPort.of(2))
        	    .build();
        	actionList1.add(output1);
        
       
        
        OFInstructions inst1=f.instructions(); 
        OFInstructionApplyActions apply1=inst1.buildApplyActions().setActions(actionList1).build();
        ArrayList<OFInstruction> instList1= new ArrayList<OFInstruction>();
        instList1.add(apply1);
        OFFlowMod.Builder fmb1= factory.buildFlowAdd();
        OFFlowMod msg1 = fmb1.setPriority(32769)
        .setMatch(m1)
        .setInstructions(instList1)
        .setOutPort(OFPort.of(2))
        .build();

        switchService.getSwitch(switchId).write(msg);
			switchService.getSwitch(switchId).write(msg1);
			
			
			
			
			 

        System.out.println("Switch listener started");
	}

	@Override
	public void switchRemoved(DatapathId switchId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void switchActivated(DatapathId switchId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void switchPortChanged(DatapathId switchId, OFPortDesc port, PortChangeType type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void switchChanged(DatapathId switchId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void switchDeactivated(DatapathId switchId) {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// TODO Auto-generated method stub
		return null;
	}



}
