package net.fabricmc.fabric.impl.attachment;

import java.util.IdentityHashMap;
import java.util.function.Function;

import net.neoforged.neoforge.attachment.AttachmentType;

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget.OnAttachedSet;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class AttachmentChangeEvents {
	private static final IdentityHashMap<AttachmentType<?>, Event<OnAttachedSet<?>>> LISTENERS = new IdentityHashMap<>();

	@SuppressWarnings("unchecked")
	public static <A> Event<OnAttachedSet<A>> onAttachedSet(net.fabricmc.fabric.api.attachment.v1.AttachmentType<A> type) {
		net.neoforged.neoforge.attachment.AttachmentType<A> neoType = ((AttachmentTypeImpl<A>) type).internalType();
		return (Event<OnAttachedSet<A>>) (Event<?>) LISTENERS.computeIfAbsent(neoType, t -> {
			return (Event<OnAttachedSet<?>>) (Event<?>) EventFactory.createArrayBacked(OnAttachedSet.class, (Function<OnAttachedSet<A>[], OnAttachedSet<A>>) listeners -> (oldValue, newValue) -> {
				for (OnAttachedSet<A> listener : listeners) {
					listener.onAttachedSet(oldValue, newValue);
				}
			});
		});
	}

	@SuppressWarnings("unchecked")
	public static <T> void invoke(AttachmentType<T> type, T oldValue, T value) {
		Event<OnAttachedSet<T>> event = (Event<OnAttachedSet<T>>) (Event<?>) LISTENERS.get(type);

		if (event != null) {
			event.invoker().onAttachedSet(oldValue, value);
		}
	}
}
